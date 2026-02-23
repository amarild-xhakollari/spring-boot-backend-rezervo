# Package: `com.myapp.reservations.dto`

This package contains Data Transfer Objects (DTOs) implemented as Java records. DTOs are used for all data exchange between the controller and service layers. Request DTOs carry validated client input; Response DTOs carry serialized output.

---

## Root-Level DTOs

### `SignInRequest.java`
**Purpose**: Login credentials.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `identifier` | `String` | - | Username or email (if contains `@`, treated as email) |
| `password` | `String` | - | Plain text password |

### `AuthResponse.java`
**Purpose**: Wrapper for authentication responses (also duplicated in `userdto` sub-package).

| Field | Type | Description |
|-------|------|-------------|
| `token` | `String` | JWT token |
| `user` | `UserResponse` | Authenticated user details |

### `BusyBlockResponse.java`
**Purpose**: Represents a time block where a business is unavailable (for calendar rendering).

| Field | Type | Description |
|-------|------|-------------|
| `start` | `LocalDateTime` | Block start time |
| `end` | `LocalDateTime` | Block end time |
| `type` | `String` | Block type: `"CLOSED"`, `"BREAK"`, or `"OCCUPIED"` |

---

## Sub-package: `userdto`

### `UserRequest.java`
**Purpose**: Input for creating/updating a user.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `name` | `String` | `@NotBlank` | Username |
| `email` | `String` | `@NotBlank`, `@Email` | Email address |
| `password` | `String` | `@NotBlank`, `@Size(min=8)` | Password (min 8 chars) |
| `phone` | `String` | - | Phone number (optional) |

### `UserResponse.java`
**Purpose**: Output for user data.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | User ID |
| `name` | `String` | Username |
| `email` | `String` | Email |
| `phone` | `String` | Phone |
| `roles` | `Set<String>` | User roles |
| `avatarUrl` | `String` | Avatar URL (e.g., `/uploads/avatars/...`) |

### `AuthResponse.java`
**Purpose**: Same as root-level `AuthResponse` (duplicate in this sub-package).

| Field | Type | Description |
|-------|------|-------------|
| `token` | `String` | JWT token |
| `user` | `UserResponse` | User details |

---

## Sub-package: `businessdto`

### `BusinessRequest.java`
**Purpose**: Input for creating/updating a business.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `name` | `String` | `@NotBlank` | Business name |
| `description` | `String` | `@Size(max=1000)` | Description |
| `address` | `String` | `@NotBlank` | Physical address |
| `phone` | `String` | `@NotBlank` | Contact phone |
| `businessType` | `BusinessType` | `@NotNull` | Category enum |
| `customType` | `String` | - | Custom category (when type=OTHER) |

### `BusinessResponse.java`
**Purpose**: Output for business data, including nested schedule and offerings.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Business ID |
| `name` | `String` | Business name |
| `description` | `String` | Description |
| `address` | `String` | Address |
| `phone` | `String` | Phone |
| `ownerId` | `UUID` | Owner's user ID |
| `adminIds` | `List<UUID>` | Admin user IDs |
| `schedule` | `ScheduleSettingsResponse` | Nested schedule configuration |
| `offerings` | `List<OfferingResponse>` | Nested service offerings |
| `businessType` | `BusinessType` | Category enum |
| `customType` | `String` | Custom category name |
| `category` | `String` | Human-readable display category |
| `imageUrl` | `String` | Main image URL |

---

## Sub-package: `businessphotodto`

### `BusinessPhotoResponse.java`
**Purpose**: Output for a gallery photo.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Photo ID |
| `url` | `String` | Photo URL |
| `caption` | `String` | Photo caption |
| `displayOrder` | `Integer` | Display order |
| `createdAt` | `LocalDateTime` | Upload timestamp |

---

## Sub-package: `reservationdto`

### `ReservationRequest.java`
**Purpose**: Input for creating a reservation.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `businessId` | `UUID` | `@NotNull` | Target business |
| `offeringId` | `UUID` | `@NotNull` | Service to book |
| `startTime` | `LocalDateTime` | `@NotNull` | Desired appointment start |
| `notes` | `String` | - | Optional notes |

### `ReservationResponse.java`
**Purpose**: Output for reservation data.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Reservation ID |
| `businessId` | `UUID` | Business ID |
| `businessName` | `String` | Business name |
| `offeringId` | `UUID` | Offering ID |
| `offeringName` | `String` | Offering name |
| `userId` | `UUID` | Customer ID |
| `userName` | `String` | Customer name |
| `startDateTime` | `LocalDateTime` | Start time |
| `endDateTime` | `LocalDateTime` | End time |
| `status` | `ReservationStatus` | PENDING / CONFIRMED / CANCELLED |
| `createdAt` | `LocalDateTime` | Creation timestamp |

### `RejectRequest.java`
**Purpose**: Optional body when rejecting a reservation.

| Field | Type | Description |
|-------|------|-------------|
| `reason` | `String` | Rejection reason (optional) |

---

## Sub-package: `reviewdto`

### `ReviewRequest.java`
**Purpose**: Input for creating a review.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `rating` | `Integer` | `@NotNull`, `@Min(1)`, `@Max(5)` | Star rating 1-5 |
| `comment` | `String` | - | Optional review text |

### `ReviewResponse.java`
**Purpose**: Output for review data.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Review ID |
| `businessId` | `UUID` | Business ID |
| `userId` | `UUID` | Reviewer's user ID |
| `userName` | `String` | Reviewer's username |
| `userAvatar` | `String` | Reviewer's avatar URL |
| `rating` | `Integer` | Star rating |
| `comment` | `String` | Review text |
| `createdAt` | `LocalDateTime` | When the review was posted |

---

## Sub-package: `notificationdto`

### `NotificationRequest.java`
**Purpose**: Input for creating a notification (used internally by services).

| Field | Type | Description |
|-------|------|-------------|
| `userId` | `UUID` | Recipient user ID |
| `title` | `String` | Notification title |
| `message` | `String` | Notification body |
| `type` | `NotificationType` | INFO / SUCCESS / ALERT / WARNING |
| `targetUrl` | `String` | URL to navigate to on click |

### `NotificationResponse.java`
**Purpose**: Output for notification data.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Notification ID |
| `title` | `String` | Title |
| `message` | `String` | Body text |
| `type` | `NotificationType` | Notification category |
| `isRead` | `boolean` | Read status |
| `targetUrl` | `String` | Navigation URL |
| `createdAt` | `LocalDateTime` | Creation timestamp |

### `UnreadCountResponse.java`
**Purpose**: Wraps unread notification count.

| Field | Type | Description |
|-------|------|-------------|
| `unreadCount` | `long` | Number of unread notifications |

---

## Sub-package: `schedulesettingsdto`

### `ScheduleSettingsRequest.java`
**Purpose**: Input for updating schedule configuration.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `reservationType` | `ReservationType` | - | SLOT or RANGE |
| `slotDurationValue` | `Integer` | `@Min(1)` | Duration number |
| `slotDurationUnit` | `ChronoUnit` | - | Duration unit (e.g., MINUTES) |
| `minAdvanceBookingHours` | `Integer` | `@Min(0)` | Minimum lead time |
| `maxAdvanceBookingDays` | `Integer` | `@Min(1)` | Maximum advance booking |
| `autoConfirmAppointments` | `Boolean` | - | Auto-confirm flag |
| `workingDays` | `List<WorkingDayRequest>` | - | Working day configurations |

### `ScheduleSettingsResponse.java`
**Purpose**: Output for schedule configuration.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Schedule ID |
| `reservationType` | `ReservationType` | SLOT or RANGE |
| `slotDurationValue` | `Integer` | Duration number |
| `slotDurationUnit` | `ChronoUnit` | Duration unit |
| `minAdvanceBookingHours` | `Integer` | Minimum lead time |
| `maxAdvanceBookingDays` | `Integer` | Maximum advance booking |
| `autoConfirmAppointments` | `Boolean` | Auto-confirm flag |
| `workingDays` | `List<WorkingDayResponse>` | Nested working day configs |

---

## Sub-package: `workingdaydto`

### `WorkingDayRequest.java`
**Purpose**: Input for a single day's schedule.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `dayOfWeek` | `String` | `@NotNull` | Day name (e.g., "MONDAY") |
| `startTime` | `LocalTime` | - | Opening time |
| `endTime` | `LocalTime` | - | Closing time |
| `breakStartTime` | `LocalTime` | - | Break start (optional) |
| `breakEndTime` | `LocalTime` | - | Break end (optional) |
| `isDayOff` | `boolean` | - | Whether business is closed |

### `WorkingDayResponse.java`
**Purpose**: Output for a single day's schedule.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Working day ID |
| `dayOfWeek` | `String` | Day name |
| `startTime` | `LocalTime` | Opening time |
| `endTime` | `LocalTime` | Closing time |
| `breakStartTime` | `LocalTime` | Break start |
| `breakEndTime` | `LocalTime` | Break end |
| `isDayOff` | `boolean` | Closed flag |

---

## Sub-package: `timeoffdto`

### `TimeOffRequest.java`
**Purpose**: Input for creating a time-off period.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `startDateTime` | `LocalDateTime` | `@NotNull` | Time-off start |
| `endDateTime` | `LocalDateTime` | `@NotNull` | Time-off end |
| `reason` | `String` | - | Reason (optional) |
| `scheduleSettingsId` | `UUID` | `@NotNull` | Parent schedule |

### `TimeOffResponse.java`
**Purpose**: Output for time-off data.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Time-off ID |
| `startDateTime` | `LocalDateTime` | Start |
| `endDateTime` | `LocalDateTime` | End |
| `reason` | `String` | Reason |

---

## Sub-package: `timeoffdto.offeringdto`

> **Note**: The `offeringdto` sub-package is nested under `timeoffdto` (appears to be a packaging anomaly).

### `OfferingRequest.java`
**Purpose**: Input for creating/updating a service offering.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `name` | `String` | `@NotBlank` | Service name |
| `description` | `String` | - | Service description |
| `price` | `Double` | `@Positive` | Price |
| `durationMinutes` | `Integer` | `@Min(5)` | Duration in minutes |
| `bufferTimeMinutes` | `Integer` | `@Min(0)` | Buffer between appointments |
| `businessId` | `UUID` | `@NotBlank` | Parent business |

### `OfferingResponse.java`
**Purpose**: Output for offering data.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Offering ID |
| `name` | `String` | Service name |
| `description` | `String` | Service description |
| `price` | `Double` | Price |
| `durationMinutes` | `Integer` | Duration |
| `bufferTimeMinutes` | `Integer` | Buffer time |
| `businessId` | `UUID` | Parent business ID |
