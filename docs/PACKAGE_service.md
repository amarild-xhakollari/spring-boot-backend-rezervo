# Package: `com.myapp.reservations.service`

This package contains all service layer classes that implement the core business logic of the reservation system. Services handle transactions, orchestrate operations between repositories, validate business rules, and coordinate entity interactions.

---

## `UserService.java`

**Purpose**: Manages user account operations including registration, authentication support, profile management, and role assignment.

**Dependencies**:
- `UserRepository` - User data access
- `PasswordEncoder` - BCrypt password hashing
- `AuthTokenFilter` - JWT token extraction (via request attributes)

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `getUsers()` | None | `List<UserResponse>` | Retrieves all users in the system |
| `findByName()` | `String name` | `UserResponse` | Finds a user by username |
| `findById()` | `UUID id` | `UserResponse` | Finds a user by ID |
| `findByEmail()` | `String email` | `UserResponse` | Finds a user by email address |
| `existsByEmail()` | `String email` | `boolean` | Checks if a user exists with the given email |
| `createUser()` | `UserRequest request` | `UserResponse` | Registers a new user with default "USER" role |
| `getUsersByRoles()` | `Role role` | `List<UserResponse>` | Retrieves all users with a specific role |
| `deleteUserById()` | `UUID id` | `void` | Deletes a user by ID (soft operation, returns silently if not found) |
| `updateUser()` | `UUID userId, UserRequest request` | `UserResponse` | Updates user profile fields (name, email, phone, password) |
| `getCurrentUserId()` | None | `UUID` | Retrieves the ID of the currently authenticated user |
| `updateAvatar()` | `UUID userId, String avatarPath` | `void` | Updates the user's avatar file path |
| `getAvatarPath()` | `UUID userId` | `String` | Retrieves the avatar path for a user |

**Business Logic**:
- **Password Security**: All passwords are BCrypt-hashed before storage
- **Default Role**: New users automatically receive the "USER" role
- **Current User Resolution**: First attempts to read user ID from request attribute (set by JWT filter), falls back to SecurityContext if not found
- **Partial Updates**: Update method only modifies non-null fields from the request

**Exceptions Thrown**:
- `IllegalArgumentException` - When required parameters are null or missing
- `UserNotFoundException` - When a user cannot be found by ID, name, or email

**Annotations**: `@Service`, `@Transactional` (on create, update, and avatar methods)

---

## `BusinessService.java`

**Purpose**: Manages business entity CRUD operations, owner/admin relationships, and access control for business resources.

**Dependencies**:
- `BusinessRepository` - Business data access
- `UserRepository` - User data access
- `ScheduleService` - Creates default schedules for new businesses

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `getBusinessById()` | `UUID businessId` | `BusinessResponse` | Retrieves a single business by ID |
| `getAllBusinesses()` | None | `List<BusinessResponse>` | Retrieves all businesses in the system |
| `getBusinessByName()` | `String name` | `BusinessResponse` | Finds a business by name |
| `getAllBusinessesByUserId()` | `UUID ownerId` | `List<BusinessResponse>` | Retrieves all businesses owned by a specific user |
| `deleteBusinessById()` | `UUID businessId` | `void` | Deletes a business and all associated entities (cascade) |
| `createBusiness()` | `BusinessRequest request, UUID currentUserId` | `BusinessResponse` | Creates a new business with default schedule |
| `updateBusiness()` | `UUID businessId, BusinessRequest request` | `BusinessResponse` | Updates business details (name, description, address, etc.) |
| `addAdminToBusiness()` | `UUID businessId, UUID userId` | `void` | Adds a user as admin to a business |
| `getAllAdmins()` | `UUID businessId` | `List<UserResponse>` | Retrieves all admin users for a business |
| `isOwnerOrAdmin()` | `UUID businessId, UUID userId` | `boolean` | Checks if a user is the owner or an admin of a business |
| `updateImage()` | `UUID businessId, String imagePath` | `void` | Updates the main business image path |
| `getImagePath()` | `UUID businessId` | `String` | Retrieves the main business image path |

**Business Logic**:
- **Auto Role Assignment**: When a user creates their first business, "BUSINESS_OWNER" role is automatically added
- **Admin Role Management**: Adding an admin to a business automatically grants the "BUSINESS_ADMIN" role
- **Default Schedule**: Every new business gets a default schedule (9-5, Mon-Fri) via `ScheduleService.createDefaultSchedule()`
- **Bidirectional Sync**: Admin relationships maintain both sides of the many-to-many relationship
- **Access Control Helper**: `isOwnerOrAdmin()` used throughout the application for authorization

**Exceptions Thrown**:
- `IllegalArgumentException` - When required parameters are null
- `BusinessNotFoundException` - When a business cannot be found
- `UserNotFoundException` - When a user cannot be found

**Annotations**: `@Service`, `@Transactional` (on create, update, addAdmin, and image methods)

---

## `ScheduleService.java`

**Purpose**: Manages business schedule settings and working hours configuration, including validation of time constraints.

**Dependencies**:
- `ScheduleSettingsRepository` - Schedule settings data access
- `WorkingDayRepository` - Working day data access
- `BusinessRepository` - Business data access

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `createDefaultSchedule()` | `Business business` | `void` | Creates default schedule for a new business (9-5, Mon-Fri, weekends off) |
| `updateSchedule()` | `UUID businessId, ScheduleSettingsRequest request` | `void` | Updates schedule settings and working hours |
| `getScheduleById()` | `UUID scheduleId` | `ScheduleSettingsResponse` | Retrieves schedule by schedule ID |
| `getScheduleByBusinessId()` | `UUID businessId` | `ScheduleSettingsResponse` | Retrieves schedule for a specific business |
| `getAllSchedules()` | None | `List<ScheduleSettingsResponse>` | Retrieves all schedules in the system |

**Business Logic**:
- **Default Configuration**: New businesses start with 30-minute slots, 2-hour minimum advance booking, 30-day maximum advance booking, and auto-confirmation enabled
- **Working Hours Validation**: Ensures start time is before end time, break times are within working hours, and break start is before break end
- **Day-by-Day Configuration**: Each day of the week has separate settings (start/end time, breaks, day-off status)
- **Partial Updates**: Only updates fields provided in the request

**Private Helper Methods**:
- `updateDayDetails()` - Updates individual working day configuration
- `validateWorkingHours()` - Validates time constraints for a working day

**Exceptions Thrown**:
- `IllegalArgumentException` - When time constraints are violated (e.g., opening time after closing time)
- `ScheduleNotFoundException` - When a schedule cannot be found for a business
- `BusinessNotFoundException` - When a business cannot be found

**Annotations**: `@Service`, `@Transactional` (on updateSchedule)

---

## `OfferingService.java`

**Purpose**: Manages service offerings (treatments, appointments types) that businesses provide to customers.

**Dependencies**:
- `OfferingRepository` - Offering data access
- `BusinessRepository` - Business data access

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `createService()` | `UUID businessId, OfferingRequest request` | `OfferingResponse` | Creates a new service offering for a business |
| `getOfferingById()` | `UUID offeringId` | `OfferingResponse` | Retrieves a single offering by ID |
| `getBusinessOfferings()` | `UUID businessId` | `List<OfferingResponse>` | Retrieves all offerings for a specific business |
| `deleteOfferingId()` | `UUID offeringId` | `void` | Deletes an offering |
| `updateOffering()` | `UUID offeringId, OfferingRequest request` | `OfferingResponse` | Updates offering details (name, price, duration, etc.) |

**Business Logic**:
- **Business Association**: Offerings are always linked to a specific business
- **Partial Updates**: Only updates non-null fields from the request
- **Duration Management**: Supports both service duration and buffer time between appointments

**Exceptions Thrown**:
- `IllegalArgumentException` - When businessId is null
- `BusinessNotFoundException` - When the specified business doesn't exist
- `OfferingNotFoundException` - When an offering cannot be found

**Annotations**: `@Service`, `@Transactional` (on create and update methods)

---

## `ReservationService.java`

**Purpose**: Manages the complete reservation lifecycle including creation, validation, confirmation, rejection, and cancellation with integrated notification system.

**Dependencies**:
- `BusinessRepository` - Business data access
- `ReservationRepository` - Reservation data access
- `ScheduleSettingsRepository` - Schedule validation
- `OfferingRepository` - Service offering data access
- `TimeOffRepository` - Business closure checking
- `UserRepository` - User data access
- `UserService` - Current user identification
- `NotificationService` - User notifications (lazy-loaded to prevent circular dependency)

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `createReservation()` | `ReservationRequest request` | `ReservationResponse` | Creates a new reservation with full validation |
| `cancelReservation()` | `UUID reservationId` | `void` | Cancels a reservation (by customer or business owner) |
| `confirmReservation()` | `UUID reservationId` | `ReservationResponse` | Confirms a pending reservation (business owner only) |
| `rejectReservation()` | `UUID reservationId, String reason` | `ReservationResponse` | Rejects a pending reservation with optional reason |
| `getMyReservations()` | `UUID userId` | `List<ReservationResponse>` | Retrieves all reservations for a specific user |
| `getReservationsByBusiness()` | `UUID businessId` | `List<ReservationResponse>` | Retrieves all reservations for a specific business |

**Business Logic**:
- **Multi-Level Validation**:
  - Working hours validation (must be within business operating hours)
  - Advance booking requirements (minimum/maximum booking window)
  - Overlap detection (prevents double-booking)
  - Time-off conflict checking (prevents booking during closures)
  - Break time validation (prevents booking during breaks)
- **Auto-Confirmation**: If business has `autoConfirmAppointments=true`, reservations are immediately confirmed; otherwise, they start as PENDING
- **Duration Calculation**: End time is automatically calculated from offering duration
- **Notification System**: Sends notifications to both business owner and customer at each status change
- **Role-Based Actions**: Only business owners can confirm/reject; customers and owners can cancel

**Private Helper Methods**:
- `validateWorkingHours()` - Ensures reservation time is within business hours and doesn't overlap breaks
- `validateAdvanceBookingRequirements()` - Checks min/max advance booking constraints

**Exceptions Thrown**:
- `RuntimeException` - Various business rule violations (closed days, outside hours, double-booking, time-off conflicts, invalid status transitions)
- `ScheduleNotFoundException` - When business schedule not found
- `BusinessNotFoundException` - When business not found
- `UserNotFoundException` - When user not found

**Date Formatting**: Uses `MMM dd, yyyy 'at' HH:mm` format for user-facing notifications

**Annotations**: `@Service`, `@Transactional` (on all mutation methods)

---

## `AvailabilityService.java`

**Purpose**: Calculates and returns "busy blocks" for a business - time periods when reservations cannot be made due to existing bookings, closures, breaks, or time-off periods.

**Dependencies**:
- `ReservationRepository` - Active reservation data
- `ScheduleSettingsRepository` - Schedule configuration
- `TimeOffRepository` - Business closure periods

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `getBusyBlocks()` | `UUID businessId, LocalDateTime viewStart, LocalDateTime viewEnd` | `List<BusyBlockResponse>` | Returns all busy/unavailable time blocks in the specified range |

**Business Logic**:
- **Closed Blocks**: Calculates when business is closed (before opening, after closing, entire days off)
- **Break Blocks**: Identifies break times within working days
- **Occupied Blocks**: Includes all confirmed/pending reservations
- **Time-Off Blocks**: Includes business-defined time-off periods (vacations, maintenance)
- **Sorted Output**: Returns blocks sorted chronologically by start time

**Block Types**:
- `CLOSED` - Business not operating (before/after hours, days off)
- `BREAK` - Break time during working hours
- `OCCUPIED` - Existing reservation or time-off period

**Private Helper Methods**:
- `calculateClosedBlocks()` - Generates closure blocks based on working day configuration

**Exceptions Thrown**:
- `ScheduleNotFoundException` - When business schedule settings not found

**Use Case**: Frontend calendar components use this to gray out unavailable time slots

**Annotations**: `@Service`

---

## `NotificationService.java`

**Purpose**: Manages in-app notifications for users including creation, retrieval, and read status tracking.

**Dependencies**:
- `NotificationRepository` - Notification data access
- `UserRepository` - User data access
- `UserService` - Current user identification

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `getLatestNotifications()` | None | `List<NotificationResponse>` | Retrieves the 10 most recent notifications for current user |
| `getUnreadCount()` | None | `long` | Returns count of unread notifications for current user |
| `markAsRead()` | `UUID notificationId` | `NotificationResponse` | Marks a single notification as read |
| `markAllAsRead()` | None | `int` | Marks all notifications as read for current user |
| `createNotification()` | `NotificationRequest request` | `NotificationResponse` | Creates a notification from a request object |
| `createNotificationForUser()` | `UUID userId, String title, String message, NotificationType type, String targetUrl` | `void` | Directly creates a notification for a specific user |

**Business Logic**:
- **User Scoping**: All retrieval methods automatically filter to current user's notifications
- **Authorization Check**: Users can only mark their own notifications as read
- **Convenience Method**: `createNotificationForUser()` is used internally by other services (ReservationService) to send notifications
- **Target URL**: Supports optional navigation URL for frontend routing

**Notification Types**:
- `INFO` - General information (reservation received)
- `SUCCESS` - Positive outcome (reservation confirmed)
- `ALERT` - Important alert (reservation rejected/cancelled by business)
- `WARNING` - Warning (customer cancelled reservation)

**Exceptions Thrown**:
- `NotificationNotFoundException` - When notification not found
- `UserNotFoundException` - When user not found
- `UnauthorizedException` - When user attempts to access another user's notification

**Annotations**: `@Service`, `@Transactional` (on all mutation methods)

---

## `ReviewService.java`

**Purpose**: Manages customer reviews for businesses with one-review-per-user-per-business constraint enforcement.

**Dependencies**:
- `ReviewRepository` - Review data access
- `BusinessRepository` - Business data access
- `UserRepository` - User data access
- `UserService` - Current user identification

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `getReviewsByBusiness()` | `UUID businessId` | `List<ReviewResponse>` | Retrieves all reviews for a business, ordered by creation date (newest first) |
| `createReview()` | `UUID businessId, ReviewRequest request` | `ReviewResponse` | Creates a new review for a business |

**Business Logic**:
- **One Review Per User**: Enforces that each user can only leave one review per business
- **Current User Association**: Reviews are automatically linked to the authenticated user
- **Chronological Ordering**: Reviews returned in reverse chronological order

**Private Helper Methods**:
- `toResponse()` - Maps Review entity to ReviewResponse DTO

**Exceptions Thrown**:
- `IllegalArgumentException` - When businessId is null
- `DuplicateReviewException` - When user attempts to create a second review for the same business
- `BusinessNotFoundException` - When business not found
- `UserNotFoundException` - When current user not found

**Annotations**: `@Service`, `@Transactional` (on createReview)

---

## `TimeOffService.java`

**Purpose**: Manages business time-off periods for vacations, maintenance, holidays, or other closures.

**Dependencies**:
- `TimeOffRepository` - Time-off data access
- `ScheduleSettingsRepository` - Schedule settings data access

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `addTimeOff()` | `UUID businessId, TimeOffRequest request` | `void` | Creates a new time-off period for a business |
| `getBusinessTimeOff()` | `UUID businessId` | `List<TimeOffResponse>` | Retrieves all time-off periods for a business |
| `deleteTimeOff()` | `UUID timeOffId` | `void` | Deletes a time-off period |

**Business Logic**:
- **Time Validation**: Ensures end time is after start time
- **Schedule Association**: Time-off periods are linked to the business's schedule settings
- **Reservation Blocking**: Time-off periods prevent new reservations (checked in ReservationService)

**Exceptions Thrown**:
- `IllegalArgumentException` - When end time is before start time
- `RuntimeException` - When schedule settings not found for business

**Annotations**: `@Service`, `@Transactional` (on addTimeOff)

**Lombok**: Uses `@RequiredArgsConstructor` for constructor injection

---

## `FileStorageService.java`

**Purpose**: Handles file upload, validation, and deletion for images (avatars, business photos, business main images).

**Dependencies**: None (standalone utility service)

**Configuration**:
- `app.upload.dir` - Base upload directory (default: "uploads")
- `ALLOWED_EXTENSIONS` - jpg, jpeg, png, gif, webp
- `MAX_FILE_SIZE` - 5MB (5,242,880 bytes)

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `storeFile()` | `MultipartFile file, String subdirectory` | `String` | Stores an uploaded file and returns relative path |
| `deleteFile()` | `String filePath` | `void` | Deletes a file from the filesystem |

**Business Logic**:
- **Validation**: Checks file is not empty, within size limit, and has allowed extension
- **Unique Naming**: Generates UUID-based filename to prevent collisions
- **Directory Management**: Automatically creates subdirectories if they don't exist
- **Safe Deletion**: Silently succeeds if file doesn't exist

**File Path Format**: Returns `subdirectory/uuid.extension` (relative to upload dir)

**Private Helper Methods**:
- `validateFile()` - Performs all file validation checks
- `getFileExtension()` - Extracts file extension from filename

**Exceptions Thrown**:
- `IllegalArgumentException` - File empty, too large, invalid type, or invalid filename
- `IOException` - File system errors during store/delete operations

**Annotations**: `@Service`

---

## `BusinessPhotoService.java`

**Purpose**: Manages business photo gallery with access control, display ordering, and maximum photo limits.

**Dependencies**:
- `BusinessPhotoRepository` - Photo data access
- `BusinessRepository` - Business data access
- `FileStorageService` - File storage operations
- `BusinessService` - Owner/admin authorization
- `UserService` - Current user identification

**Configuration**:
- `MAX_PHOTOS_PER_BUSINESS` - 20 photos per business

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `getPhotosByBusiness()` | `UUID businessId` | `List<BusinessPhotoResponse>` | Retrieves all photos for a business, ordered by displayOrder (ASC) then createdAt (DESC) |
| `addPhoto()` | `UUID businessId, String filePath, String caption` | `BusinessPhotoResponse` | Adds a new photo to business gallery |
| `deletePhoto()` | `UUID photoId` | `void` | Deletes a photo and its file from the filesystem |
| `updateCaption()` | `UUID photoId, String caption` | `BusinessPhotoResponse` | Updates a photo's caption |

**Business Logic**:
- **Authorization**: All mutations require user to be business owner or admin
- **Photo Limit**: Enforces maximum of 20 photos per business
- **Display Order**: New photos automatically get next available order number (max + 1)
- **File Cleanup**: Deleting a photo also removes the physical file
- **URL Prefix**: Photo paths returned with `/uploads/` prefix for frontend

**Private Helper Methods**:
- `toResponse()` - Maps BusinessPhoto entity to response DTO with URL prefix

**Exceptions Thrown**:
- `UnauthorizedException` - When user is not owner or admin
- `MaxPhotosReachedException` - When business already has 20 photos
- `BusinessNotFoundException` - When business not found
- `BusinessPhotoNotFoundException` - When photo not found
- `IOException` - File deletion errors

**Annotations**: `@Service`, `@Transactional` (on all mutation methods)

---

## `CustomUserDetailsService.java`

**Purpose**: Implements Spring Security's `UserDetailsService` interface to load user authentication details by username or ID.

**Dependencies**:
- `UserRepository` - User data access

### Public Methods

| Method | Parameters | Return Type | Description |
|--------|-----------|-------------|-------------|
| `loadUserByUsername()` | `String name` | `UserDetails` | Loads user by username (required by UserDetailsService interface) |
| `loadUserById()` | `UUID userId` | `UserDetails` | Loads user by ID (custom method for JWT authentication) |

**Business Logic**:
- **Spring Security Integration**: Returns Spring Security's UserDetails object for authentication
- **Empty Authorities**: Returns empty authorities list (role-based authorization not implemented at Spring Security level)
- **JWT Support**: `loadUserById()` used by JWT filter to load user from token claims

**Exceptions Thrown**:
- `UsernameNotFoundException` - When user not found by username or ID

**Annotations**: `@Service`

**Note**: This service is specifically for authentication. Authorization is handled at the application level using custom checks (e.g., `BusinessService.isOwnerOrAdmin()`).

---

## Service Layer Architecture Notes

### Transaction Management
Most mutation methods are marked with `@Transactional` to ensure data consistency. Read-only operations generally don't require explicit transaction annotations.

### Circular Dependency Resolution
`ReservationService` uses `@Lazy` injection for `NotificationService` to prevent circular dependency (since NotificationService depends on UserService, which could create cycles).

### Mapper Pattern
Services use dedicated Mapper classes (e.g., `UserMapper`, `BusinessMapper`) to convert between entities and DTOs, keeping the service layer focused on business logic.

### Error Handling
Services throw specific exception types (e.g., `UserNotFoundException`, `BusinessNotFoundException`) which are caught and handled by global exception handlers in the controller layer.

### Security Context
Several services (`UserService`, `NotificationService`, `ReviewService`, etc.) use `getCurrentUserId()` to automatically associate operations with the authenticated user, simplifying controller code.

### Validation Strategy
- **Input Validation**: Null checks with `IllegalArgumentException` for required parameters
- **Business Rules**: Runtime exceptions with descriptive messages for constraint violations
- **Entity Validation**: JPA validation annotations on entities provide additional safety

### Dependency Injection
All services use constructor-based dependency injection (some with Lombok's `@RequiredArgsConstructor`) following Spring best practices.
