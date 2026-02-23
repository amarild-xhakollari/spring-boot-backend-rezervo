# Rezervo - Backend Documentation (General Overview)

## 1. Project Summary

**Rezervo** is a reservation/booking management platform built with Spring Boot. It allows business owners to register their businesses (salons, barbershops, spas, fitness centers, etc.), define service offerings, configure working schedules, and receive reservations from customers. Customers can browse businesses, book appointments, leave reviews, and manage their profiles.

### Tech Stack

| Component         | Technology                              |
|-------------------|-----------------------------------------|
| Language          | Java 21                                 |
| Framework         | Spring Boot 3.4.12                      |
| Build Tool        | Maven 3                                 |
| Database          | PostgreSQL 16 (via Docker Compose)      |
| ORM               | Spring Data JPA / Hibernate             |
| Authentication    | JWT (jjwt 0.12.6)                       |
| Security          | Spring Security (stateless, BCrypt)     |
| Validation        | Spring Boot Starter Validation (Jakarta)|
| API Docs          | SpringDoc OpenAPI (Swagger UI) 2.5.0    |
| Dev Tools         | Spring Boot DevTools, Lombok            |
| Containerization  | Docker Compose (PostgreSQL)             |
| Testing           | Spring Boot Test, Spring Security Test  |
| CORS              | Configured for `http://localhost:5173`   |

### Infrastructure

- **Database**: PostgreSQL 16 runs via Docker Compose on port `5442` (mapped to container port `5432`). DB name: `reservations`, user: `user1`.
- **Server**: Spring Boot runs on port `8080`.
- **File Storage**: Local file system (`uploads/` directory) for avatars, business images, and gallery photos.
- **Frontend**: Expected at `http://localhost:5173` (Vite-based frontend).

---

## 2. Architecture Overview

The project follows a **layered architecture** pattern:

```
Client (Frontend)
      |
      v
  Controller Layer        (REST API endpoints - receives HTTP requests)
      |
      v
  Service Layer           (Business logic, validation, orchestration)
      |
      v
  Repository Layer        (Data access via Spring Data JPA)
      |
      v
  Entity Layer            (JPA entities mapped to PostgreSQL tables)
```

### Cross-Cutting Concerns
- **Security**: JWT-based authentication filter intercepts all requests (except public endpoints).
- **Exception Handling**: A global `@RestControllerAdvice` catches and maps exceptions to structured HTTP error responses.
- **DTO/Mapper Pattern**: All data exchanged between controller and service layers uses DTOs (Java records). Mapper classes convert between entities and DTOs.

---

## 3. Application Flows

### 3.1 Authentication Flow

```
1. User signs up:  POST /api/auth/signup
   - UserRequest (name, email, password, phone) is received
   - Password is hashed with BCrypt
   - User is saved with role "USER"
   - JWT token is generated containing username + userId
   - Returns: AuthResponse (token + UserResponse)

2. User signs in:  POST /api/auth/signin
   - SignInRequest (identifier, password) is received
   - Identifier can be username OR email (if contains "@", email lookup is done first)
   - Spring Security AuthenticationManager authenticates credentials
   - JWT token is generated
   - Returns: AuthResponse (token + UserResponse)

3. Authenticated requests:
   - Client sends JWT in "Authorization: Bearer <token>" header
   - AuthTokenFilter extracts and validates the JWT
   - UserId is extracted from token and set as request attribute
   - SecurityContext is populated with authenticated user
```

### 3.2 Business Management Flow

```
1. Create Business:  POST /api/businesses/create
   - Authenticated user provides BusinessRequest (name, description, address, phone, businessType)
   - A default ScheduleSettings is auto-created (Mon-Fri 9:00-17:00, Sat-Sun off)
   - User's role is upgraded to "BUSINESS_OWNER" if not already
   - Returns: BusinessResponse with schedule and offerings included

2. Browse Businesses:  GET /api/businesses (PUBLIC - no auth required)
   - Returns all businesses with their schedules and offerings

3. Update Business:  PUT /api/businesses/update/{id}
   - Partial updates supported (only non-null fields are updated)

4. Manage Admins:
   - GET /api/businesses/{businessId}/admins  - list admins
   - PUT /api/businesses/{businessId}/admins  - add admin (user gets "BUSINESS_ADMIN" role)
```

### 3.3 Schedule & Offerings Flow

```
1. Schedule Configuration:
   - When a business is created, a default schedule is auto-generated:
     * 7 WorkingDay entries (one per day of week)
     * Mon-Fri: 09:00-17:00, Sat-Sun: day off
     * Default slot duration: 30 minutes
     * Auto-confirm: true, min advance: 2 hours, max advance: 30 days

   - PUT /api/schedules/business/{businessId}
     * Update schedule settings (reservation type, slot duration, booking rules)
     * Update individual working days (start/end times, break times, day off flag)
     * Validates: opening before closing, break within working hours

2. Service Offerings (what a business provides):
   - POST /api/offerings/{businessId}     - create offering
   - GET /api/offerings/business/{businessId}  - list offerings (PUBLIC)
   - PUT /api/offerings/update/{offeringId}    - update offering
   - DELETE /api/offerings/{offeringId}        - delete offering
   - Each offering has: name, description, price, durationMinutes, bufferTimeMinutes

3. Time Off:
   - POST /api/time-off/business/{businessId}  - add time off period
   - GET /api/time-off/business/{businessId}   - list time offs
   - DELETE /api/time-off/{timeOffId}          - remove time off
```

### 3.4 Reservation Flow (Core Business Flow)

```
1. Customer creates reservation:  POST /api/reservations/create
   Input: ReservationRequest (businessId, offeringId, startTime)

   Validation chain:
   a) Offering is looked up -> endTime = startTime + offering.durationMinutes
   b) Schedule settings are fetched for the business
   c) Working hours validated:
      - Must be on a working day (not a day off)
      - Must be within start/end times
      - Must not overlap with break time
   d) Advance booking rules validated:
      - Cannot be in the past
      - Must respect minAdvanceBookingHours (e.g., at least 2 hours ahead)
      - Must respect maxAdvanceBookingDays (e.g., not more than 30 days ahead)
   e) Overlap check: no existing active reservation in that time slot
   f) Time-off check: business not on time off during requested slot

   If auto-confirm is ON:
   - Reservation status = CONFIRMED
   - Notification to business owner: "New Reservation Confirmed"
   - Notification to customer: "Reservation Confirmed"

   If auto-confirm is OFF:
   - Reservation status = PENDING
   - Notification to business owner: "New Reservation Request" (asks to review)
   - Notification to customer: "Reservation Received" (will be reviewed)

2. Business owner confirms:  PATCH /api/reservations/{id}/confirm
   - Only business owner can confirm
   - Only PENDING reservations can be confirmed
   - Status changes to CONFIRMED
   - Customer receives "Reservation Confirmed" notification

3. Business owner rejects:  PATCH /api/reservations/{id}/reject
   - Only business owner can reject
   - Only PENDING reservations can be rejected
   - Status changes to CANCELLED
   - Customer receives "Reservation Rejected" notification (with optional reason)

4. Cancel reservation:  PATCH /api/reservations/{id}/cancel
   - Both customer and business owner can cancel
   - Already cancelled reservations cannot be cancelled again
   - If customer cancels -> business owner is notified
   - If business owner cancels -> customer is notified

5. View reservations:
   - GET /api/reservations/mine         - customer's own reservations
   - GET /api/reservations/business/{id} - all reservations for a business
```

### 3.5 Availability Flow

```
GET /api/availabilities/busy-blocks/{businessId}?start=...&end=...

This endpoint returns a list of "busy blocks" for a calendar view:
- CLOSED blocks: time outside working hours (before open, after close, full day-off days)
- BREAK blocks: break periods within working hours
- OCCUPIED blocks: existing confirmed/pending reservations
- OCCUPIED blocks: time-off periods

The frontend uses these to render unavailable slots on a calendar.
```

### 3.6 Notification Flow

```
Notifications are created automatically by the system during:
- Reservation creation (both owner and customer get notified)
- Reservation confirmation (customer notified)
- Reservation rejection (customer notified)
- Reservation cancellation (the other party is notified)

Each notification has:
- title, message, type (INFO/SUCCESS/ALERT/WARNING), targetUrl, isRead

API:
- GET /api/notifications              - latest 10 notifications for current user
- GET /api/notifications/unread-count  - count of unread notifications
- PATCH /api/notifications/{id}/read   - mark single notification as read
- PATCH /api/notifications/read-all    - mark all as read
```

### 3.7 Review Flow

```
- GET /api/reviews/{businessId}    - list reviews for a business (PUBLIC)
- POST /api/reviews/{businessId}   - create review (authenticated)
  * Rating: 1-5 (required)
  * Comment: optional (max 1000 chars)
  * One review per user per business (DuplicateReviewException if repeated)
```

### 3.8 File Upload Flow

```
Avatars:
- POST api/files/user-avatar         - upload user avatar
- DELETE api/files/user-avatar        - delete user avatar

Business Images:
- POST api/files/business-image/{id}  - upload business main image (owner/admin only)
- DELETE api/files/business-image/{id} - delete business main image

Business Photo Gallery:
- GET api/files/business-photos/{id}   - list photos (PUBLIC)
- POST api/files/business-photos/{id}  - upload photo (owner/admin, max 20 per business)
- DELETE api/files/business-photos/{photoId} - delete photo
- PATCH api/files/business-photos/{photoId}/caption - update caption

File constraints:
- Max size: 5MB
- Allowed extensions: jpg, jpeg, png, gif, webp
- Stored in local filesystem under uploads/ directory
- Served via /uploads/** static resource handler
```

### 3.9 User Self-Management Flow

```
/api/me endpoints (any authenticated user):
- GET /api/me                - get my profile
- PUT /api/me                - update my profile
- DELETE /api/me             - delete my account
- POST /api/me/business      - create a business as current user
- GET /api/me/businesses      - list my businesses
```

### 3.10 Admin User Management Flow

```
/api/users endpoints (ADMIN role only):
- GET /api/users              - list all users
- GET /api/users/{id}         - get user by ID
- POST /api/users/create      - create user
- GET /api/users/by-name/{name} - find by name
- GET /api/users/by-role/{role} - find by role
- DELETE /api/users/{id}      - delete user
- PUT /api/users/update/{id}  - update user
```

---

## 4. Security Model

### Public Endpoints (no authentication required)
- `POST /api/auth/signin` and `POST /api/auth/signup`
- `GET /api/businesses/**` - browse businesses
- `GET /api/offerings/**` - browse offerings
- `GET /api/availabilities/**` - check availability
- `GET /api/schedules/**` - view schedules
- `GET /api/reviews/**` - read reviews
- `GET /api/files/business-photos/**` - view gallery
- `GET /uploads/**` - static file access

### Authenticated Endpoints (JWT required)
- All other endpoints require a valid JWT token

### Role-Based Access
- `USER` - basic authenticated user
- `BUSINESS_OWNER` - auto-assigned when user creates a business
- `BUSINESS_ADMIN` - assigned when added as admin to a business
- `ADMIN` - system administrator (required for `/api/users` endpoints)

### Authorization Logic
- Business operations (update image, add photos, manage admins) check `isOwnerOrAdmin()`
- Reservation confirmation/rejection: only business owner
- Notifications: user can only read/modify their own

---

## 5. Database Schema (Entity Relationships)

```
User (users)
  |-- 1:N --> Business (businesses)           [owner]
  |-- N:M --> Business (b_admins)             [admin relationship]
  |-- 1:N --> Reservation (reservations)      [customer]
  |-- 1:N --> Review (reviews)                [reviewer]
  |-- 1:N --> Notification (notifications)    [recipient]
  |-- roles stored in user_roles table (ElementCollection)

Business (businesses)
  |-- 1:1 --> ScheduleSettings                [schedule configuration]
  |-- 1:N --> Offering (offerings)            [services offered]
  |-- 1:N --> BusinessPhoto (business_photos) [gallery]
  |-- 1:N --> Reservation (reservations)      [reservations at this business]
  |-- 1:N --> Review (reviews)                [reviews of this business]

ScheduleSettings
  |-- 1:N --> WorkingDay                      [7 entries, one per day of week]
  |-- 1:N --> TimeOff (time_off)              [vacation/maintenance periods]

Reservation
  |-- N:1 --> Business
  |-- N:1 --> User (customer)
  |-- N:1 --> Offering (service booked)
```

---

## 6. Project Package Structure

```
com.myapp.reservations
├── config/              # Spring MVC configuration (CORS, static resources)
├── controller/          # REST API controllers (13 controllers)
├── dto/                 # Data Transfer Objects (Java records)
│   ├── businessdto/
│   ├── businessphotodto/
│   ├── notificationdto/
│   ├── reservationdto/
│   ├── reviewdto/
│   ├── schedulesettingsdto/
│   ├── timeoffdto/
│   │   └── offeringdto/
│   ├── userdto/
│   └── workingdaydto/
├── entities/            # JPA entities
│   ├── businessentity/
│   ├── businessSchedule/
│   ├── notification/
│   ├── reservation/
│   ├── review/
│   └── user/
├── exception/           # Exception hierarchy & global handler
│   ├── businessruleviolations/
│   ├── conflictexceptions/
│   └── notfoundexceptions/
├── mapper/              # Entity <-> DTO conversion
├── repository/          # Spring Data JPA repositories
├── security/            # JWT auth, security config, filters
└── service/             # Business logic services
```
