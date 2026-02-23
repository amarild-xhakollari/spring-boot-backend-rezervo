# Package: `com.myapp.reservations.controller`

This package contains all REST API controllers that handle HTTP requests. Each controller is annotated with `@RestController` and delegates business logic to the service layer.

---

## `AuthenticationController.java`
**Base Path**: `/api/auth`
**CORS**: `@CrossOrigin(origins = "*")`
**Purpose**: Handles user authentication (sign in, sign up) and profile retrieval.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| POST | `/signin` | Public | `SignInRequest` | `AuthResponse` | Sign in with username or email + password. If identifier contains `@`, looks up email first. |
| POST | `/signup` | Public | `UserRequest` | `AuthResponse` | Register new user. Checks for existing name/email. Returns JWT + user. |
| GET | `/profile` | Required | - | `UserResponse` | Get current authenticated user's profile |

### Dependencies
`AuthenticationManager`, `UserRepository`, `JwtUtil`, `UserService`

---

## `BusinessController.java`
**Base Path**: `/api/businesses`
**Purpose**: CRUD operations for businesses and admin management.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/` | Public | - | `List<BusinessResponse>` | List all businesses |
| GET | `/by-business-id/{id}` | Public | - | `BusinessResponse` | Get business by ID |
| GET | `/by-business-name/{name}` | Public | - | `BusinessResponse` | Get business by name |
| GET | `/by-business-owners-id/{owner_id}` | Public | - | `List<BusinessResponse>` | List businesses by owner ID |
| POST | `/create` | Required | `BusinessRequest` | `BusinessResponse` | Create a business (current user becomes owner) |
| PUT | `/update/{id}` | Required | `BusinessRequest` | `BusinessResponse` | Update a business |
| DELETE | `/{id}` | Required | - | void | Delete a business |
| GET | `/{businessId}/admins` | Required | - | `List<UserResponse>` | List business admins |
| PUT | `/{businessId}/admins` | Required | `UUID` (userId) | void | Add admin to business |

### Dependencies
`BusinessService`, `UserService`

---

## `UserController.java`
**Base Path**: `/api/users`
**Authorization**: `@PreAuthorize("hasRole('ADMIN')")` - All endpoints require ADMIN role
**Purpose**: Admin-only user management.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/` | ADMIN | - | `List<UserResponse>` | List all users |
| GET | `/{id}` | ADMIN | - | `UserResponse` | Get user by ID |
| POST | `/create` | ADMIN | `UserRequest` | `UserResponse` | Create a user |
| GET | `/by-name/{name}` | ADMIN | - | `UserResponse` | Find user by name |
| GET | `/by-role/{role}` | ADMIN | - | `List<UserResponse>` | Find users by role |
| DELETE | `/{id}` | ADMIN | - | void | Delete user |
| PUT | `/update/{id}` | ADMIN | `UserRequest` | void | Update user |

### Dependencies
`UserService`

---

## `UserSelfController.java`
**Base Path**: `/api/me`
**Authorization**: `@PreAuthorize("hasAnyRole('USER','BUSINESS_OWNER','BUSINESS_ADMIN','ADMIN')")` - Any authenticated user
**Purpose**: Self-service profile and business management for the current user.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/` | Required | - | `UserResponse` | Get own profile |
| PUT | `/` | Required | `UserRequest` | void | Update own profile |
| DELETE | `/` | Required | - | void | Delete own account |
| POST | `/business` | Required | `BusinessRequest` | `BusinessResponse` | Create a business as current user |
| GET | `/businesses` | Required | - | `List<BusinessResponse>` | List own businesses |

### Dependencies
`UserService`, `BusinessService`

---

## `ReservationController.java`
**Base Path**: `/api/reservations`
**Purpose**: Reservation lifecycle management (create, cancel, confirm, reject, list).

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| POST | `/create` | Required | `ReservationRequest` | `ReservationResponse` | Create a reservation |
| PATCH | `/{id}/cancel` | Required | - | `204 No Content` | Cancel a reservation |
| GET | `/mine` | Required | - | `List<ReservationResponse>` | Get current user's reservations |
| GET | `/business/{businessId}` | Required | - | `List<ReservationResponse>` | Get all reservations for a business |
| PATCH | `/{id}/confirm` | Required | - | `ReservationResponse` | Confirm a pending reservation (owner only) |
| PATCH | `/{id}/reject` | Required | `RejectRequest` (optional) | `ReservationResponse` | Reject a pending reservation with optional reason |

### Dependencies
`ReservationService`, `UserService`

---

## `ScheduleController.java`
**Base Path**: `/api/schedules`
**Purpose**: Schedule settings retrieval and updates.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/` | Public | - | `List<ScheduleSettingsResponse>` | List all schedules |
| GET | `/business/{businessId}` | Public | - | `ScheduleSettingsResponse` | Get schedule by business ID |
| GET | `/{scheduleId}` | Public | - | `ScheduleSettingsResponse` | Get schedule by its own ID |
| PUT | `/business/{businessId}` | Required | `ScheduleSettingsRequest` | `String` message | Update schedule and working days |

### Dependencies
`ScheduleService`

---

## `OfferingController.java`
**Base Path**: `/api/offerings`
**Purpose**: CRUD operations for service offerings.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| POST | `/{businessId}` | Required | `OfferingRequest` | `OfferingResponse` | Create offering for a business |
| GET | `/{offeringId}` | Public | - | `OfferingResponse` | Get offering by ID |
| GET | `/business/{businessId}` | Public | - | `List<OfferingResponse>` | List offerings for a business |
| PUT | `/update/{offeringId}` | Required | `OfferingRequest` | `OfferingResponse` | Update an offering |
| DELETE | `/{offeringId}` | Required | - | void | Delete an offering |

### Dependencies
`OfferingService`

---

## `TimeOffController.java`
**Base Path**: `/api/time-off`
**Purpose**: Manage business time-off periods.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| POST | `/business/{businessId}` | Required | `TimeOffRequest` | `String` message | Add time-off period |
| GET | `/business/{businessId}` | Required | - | `List<TimeOffResponse>` | List time-off periods for a business |
| DELETE | `/{timeOffId}` | Required | - | void | Delete a time-off period |

### Dependencies
`TimeOffService`

---

## `NotificationController.java`
**Base Path**: `/api/notifications`
**Purpose**: Notification retrieval and read-status management.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/` | Required | - | `List<NotificationResponse>` | Get latest 10 notifications |
| GET | `/unread-count` | Required | - | `UnreadCountResponse` | Get count of unread notifications |
| PATCH | `/{id}/read` | Required | - | `NotificationResponse` | Mark single notification as read |
| PATCH | `/read-all` | Required | - | `200 OK` (empty) | Mark all notifications as read |

### Dependencies
`NotificationService`

---

## `ReviewController.java`
**Base Path**: `/api/reviews`
**Purpose**: Business review management.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/{businessId}` | Public | - | `List<ReviewResponse>` | Get all reviews for a business |
| POST | `/{businessId}` | Required | `ReviewRequest` | `ReviewResponse` | Create a review for a business |

### Dependencies
`ReviewService`

---

## `AvailabilityController.java`
**Base Path**: `/api/availabilities`
**Purpose**: Provides busy-block data for calendar/scheduling views.

### Endpoints

| Method | Path | Auth | Request Body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| GET | `/busy-blocks/{businessId}?start=...&end=...` | Public | - | `List<BusyBlockResponse>` | Get busy blocks (closed, break, occupied) for a date range |

**Query Parameters**:
- `start` (`LocalDateTime`, ISO format) - Range start
- `end` (`LocalDateTime`, ISO format) - Range end

### Dependencies
`AvailabilityService`

---

## `FileController.java`
**Base Path**: `/api/files`
**Purpose**: File upload/delete operations for user avatars, business images, and photo galleries.

### Endpoints

| Method | Path | Auth | Request | Response | Description |
|--------|------|------|---------|----------|-------------|
| POST | `/user-avatar` | Required | `MultipartFile` | `{path, url}` | Upload user avatar |
| DELETE | `/user-avatar` | Required | - | `{message}` | Delete user avatar |
| POST | `/business-image/{businessId}` | Owner/Admin | `MultipartFile` | `{path, url}` | Upload business main image |
| DELETE | `/business-image/{businessId}` | Owner/Admin | - | `{message}` | Delete business main image |
| GET | `/business-photos/{businessId}` | Public | - | `List<BusinessPhotoResponse>` | List business gallery photos |
| POST | `/business-photos/{businessId}` | Required | `MultipartFile` + optional `caption` | `BusinessPhotoResponse` | Upload gallery photo |
| DELETE | `/business-photos/{photoId}` | Required | - | `{message}` | Delete gallery photo |
| PATCH | `/business-photos/{photoId}/caption` | Required | `{caption: "..."}` | `BusinessPhotoResponse` | Update photo caption |

### Dependencies
`FileStorageService`, `UserService`, `BusinessService`, `BusinessPhotoService`

---

## `MainController.java`
**Base Path**: `/api`
**Purpose**: Simple test/demo endpoints for verifying API and JWT access.

### Endpoints

| Method | Path | Auth | Response | Description |
|--------|------|------|----------|-------------|
| GET | `/welcome` | Unknown | `String` | Public test endpoint |
| GET | `/user` | Required | `String` | JWT-protected test endpoint |
| GET | `/special` | Required | `String` | JWT-protected test endpoint |
