# Package: `com.myapp.reservations.entities`

This package contains all JPA entity classes that map to PostgreSQL database tables. Entities are organized into sub-packages by domain.

---

## Sub-package: `entities.user`

### `User.java`
**Table**: `users`
**Purpose**: Represents a user account in the system (customer, business owner, or admin).

| Field | Column | Type | Constraints | Description |
|-------|--------|------|-------------|-------------|
| `id` | `user_id` | `UUID` | PK, unique, not null, auto-generated | Unique identifier |
| `name` | `user_name` | `String` | Not null, unique | Username for login |
| `email` | `user_email` | `String` | Not null, unique | Email address |
| `password` | `user_password` | `String` | Not null | BCrypt-hashed password |
| `roles` | `user_roles` (separate table) | `Set<String>` | Eager fetched | Roles: USER, BUSINESS_OWNER, BUSINESS_ADMIN, ADMIN |
| `phone` | `user_phone` | `String` | Optional | Phone number |
| `avatarPath` | `avatar_path` | `String` | Optional | Path to uploaded avatar image |
| `createdAt` | `created_at` | `LocalDateTime` | Auto-set | Creation timestamp |
| `updatedAt` | `updated_at` | `LocalDateTime` | Auto-set | Last update timestamp |

**Relationships**:
- `@OneToMany(mappedBy = "owner")` -> `List<Business> ownedBusinesses` - Businesses this user owns
- `@ManyToMany` via join table `b_admins` -> `List<Business> adminOfBusinesses` - Businesses this user administrates

**Annotations**: `@Entity`, `@Table`, `@NoArgsConstructor`, `@Getter`, `@Setter`, `@AllArgsConstructor` (Lombok)

**Lifecycle Callbacks**:
- `@PrePersist onCreate()`: Sets `createdAt` and `updatedAt` to current time
- `@PreUpdate onUpdate()`: Updates `updatedAt` to current time

**Notes**:
- Roles are stored as strings in a separate `user_roles` collection table (not as enum entities)
- The `@ElementCollection(fetch = FetchType.EAGER)` ensures roles are always loaded with the user
- Password is stored as BCrypt hash (hashing happens in `UserService.createUser()`)

---

### `Role.java`
**Type**: Enum
**Purpose**: Defines the available user roles in the system.

| Value | Description |
|-------|-------------|
| `USER` | Default role for registered users |
| `BUSINESS_OWNER` | Auto-assigned when a user creates a business |
| `BUSINESS_ADMIN` | Assigned when a user is added as admin to a business |
| `ADMIN` | System administrator with access to user management endpoints |

**Note**: Although this enum exists, roles are stored as `Set<String>` in the User entity, not as enum values directly. The enum is used primarily for the `getUsersByRoles()` query parameter.

---

## Sub-package: `entities.businessentity`

### `Business.java`
**Table**: `businesses`
**Purpose**: Represents a service-providing business (salon, barbershop, spa, etc.).

| Field | Column | Type | Constraints | Description |
|-------|--------|------|-------------|-------------|
| `id` | `b_id` | `UUID` | PK, unique, not null | Unique identifier |
| `name` | `b_name` | `String` | Max 100, unique | Business name |
| `description` | `b_description` | `String` | Max 1000 | Business description |
| `address` | `b_address` | `String` | Optional | Physical address |
| `phone` | `b_phone` | `String` | Optional | Contact phone |
| `businessType` | `business_type` | `BusinessType` (enum) | Stored as STRING | Category of business |
| `customType` | `custom_type` | `String` | Optional | Custom category name (when businessType=OTHER) |
| `imagePath` | `image_path` | `String` | Optional | Path to main business image |
| `createdAt` | `created_at` | `LocalDateTime` | Auto-set | Creation timestamp |
| `updatedAt` | `updated_at` | `LocalDateTime` | Auto-set | Last update timestamp |

**Relationships**:
- `@ManyToOne` -> `User owner` (column `b_owner_id`, not null) - The user who owns this business
- `@ManyToMany(mappedBy = "adminOfBusinesses")` -> `List<User> admins` - Admin users
- `@OneToOne(cascade = ALL, orphanRemoval = true)` -> `ScheduleSettings scheduleSettings` (column `schedule_settings_id`) - Schedule configuration
- `@OneToMany(mappedBy = "business", cascade = ALL, orphanRemoval = true)` -> `List<Offering> offerings` - Services offered
- `@OneToMany(mappedBy = "business", cascade = ALL, orphanRemoval = true)` -> `List<BusinessPhoto> photos` - Photo gallery (ordered by displayOrder ASC, createdAt DESC)

**Helper Methods**:
- `addOffering(Offering)`: Adds an offering and sets the back-reference

**Lifecycle Callbacks**: Same as User (`@PrePersist`, `@PreUpdate`)

---

### `BusinessPhoto.java`
**Table**: `business_photos`
**Purpose**: Represents a photo in a business's gallery.

| Field | Column | Type | Constraints | Description |
|-------|--------|------|-------------|-------------|
| `id` | `photo_id` | `UUID` | PK, unique, not null | Unique identifier |
| `business` | `business_id` | `Business` (FK) | Not null, lazy fetched | Parent business |
| `filePath` | `file_path` | `String` | Not null | Path to the uploaded image file |
| `caption` | `caption` | `String` | Max 255 | Optional description of the photo |
| `displayOrder` | `display_order` | `Integer` | Default 0 | Order for display (lower = first) |
| `createdAt` | `created_at` | `LocalDateTime` | Auto-set | Upload timestamp |

**Lifecycle Callbacks**:
- `@PrePersist onCreate()`: Sets `createdAt` and defaults `displayOrder` to 0 if null

---

### `BusinessType.java`
**Type**: Enum
**Purpose**: Categorizes businesses.

| Value | Display Name |
|-------|-------------|
| `SPA_WELLNESS` | Spa & Wellness |
| `BARBERSHOP` | Barbershop |
| `BEAUTY_SALON` | Beauty Salon |
| `FITNESS` | Fitness |
| `YOGA_MEDITATION` | Yoga & Meditation |
| `PET_SERVICES` | Pet Services |
| `OTHER` | Uses `customType` field value |

---

## Sub-package: `entities.businessSchedule`

### `ScheduleSettings.java`
**Table**: `schedule_settings`
**Purpose**: Configures how a business handles reservations and scheduling.

| Field | Column | Type | Description |
|-------|--------|------|-------------|
| `id` | `id` | `UUID` | PK |
| `reservationType` | `reservation_type` | `ReservationType` (enum) | SLOT or RANGE |
| `slotDurationValue` | `slot_duration_value` | `Integer` | Duration number (e.g., 30) |
| `slotDurationUnit` | `slot_duration_unit` | `ChronoUnit` (enum) | Duration unit (e.g., MINUTES) |
| `minAdvanceBookingHours` | `min_advance_booking_hours` | `Integer` | Minimum hours before appointment |
| `maxAdvanceBookingDays` | `max_advance_booking_days` | `Integer` | Maximum days in advance to book |
| `autoConfirmAppointments` | `auto_confirm_appointments` | `Boolean` | If true, reservations are auto-confirmed |

**Relationships**:
- `@OneToMany(mappedBy = "scheduleSettings", cascade = ALL)` -> `List<WorkingDay> workingDays` - 7 entries, one per day of week
- `@OneToOne(mappedBy = "scheduleSettings")` -> `Business business` - The business this schedule belongs to

---

### `WorkingDay.java`
**Table**: `working_day`
**Purpose**: Defines working hours for a specific day of the week.

| Field | Column | Type | Description |
|-------|--------|------|-------------|
| `id` | `id` | `UUID` | PK |
| `dayOfWeek` | `day_of_week` | `DayOfWeek` (enum) | MONDAY through SUNDAY |
| `startTime` | `start_time` | `LocalTime` | Business opens at |
| `endTime` | `end_time` | `LocalTime` | Business closes at |
| `breakStartTime` | `break_start_time` | `LocalTime` | Break starts at (optional) |
| `breakEndTime` | `break_end_time` | `LocalTime` | Break ends at (optional) |
| `isDayOff` | `is_day_off` | `boolean` | If true, business is closed this day |

**Relationships**:
- `@ManyToOne` -> `ScheduleSettings scheduleSettings` (FK: `schedule_settings_id`)

---

### `Offering.java`
**Table**: `offerings`
**Purpose**: Represents a service that a business offers (e.g., "Men's Haircut", "Swedish Massage").

| Field | Column | Type | Constraints | Description |
|-------|--------|------|-------------|-------------|
| `id` | `id` | `UUID` | PK | Unique identifier |
| `name` | `name` | `String` | Not null | Service name |
| `description` | `description` | `String` | Optional | Service description |
| `price` | `price` | `Double` | Not null | Price in local currency |
| `durationMinutes` | `duration_minutes` | `Integer` | Not null | How long the service takes |
| `bufferTimeMinutes` | `buffer_time_minutes` | `Integer` | Optional | Buffer time between appointments |

**Relationships**:
- `@ManyToOne(fetch = LAZY)` -> `Business business` (FK: `b_id`, not null, `@JsonIgnore`)

---

### `TimeOff.java`
**Table**: `time_off`
**Purpose**: Represents a period when the business is unavailable (vacation, maintenance, etc.).

| Field | Column | Type | Description |
|-------|--------|------|-------------|
| `id` | `id` | `UUID` | PK |
| `startDateTime` | `start_date_time` | `LocalDateTime` | Start of time-off period |
| `endDateTime` | `end_date_time` | `LocalDateTime` | End of time-off period |
| `reason` | `reason` | `String` | Why the business is off |

**Relationships**:
- `@ManyToOne` -> `ScheduleSettings scheduleSettings` (FK: `schedule_settings_id`)

---

## Sub-package: `entities.reservation`

### `Reservation.java`
**Table**: `reservation`
**Purpose**: Represents a customer's booking at a business.

| Field | Column | Type | Description |
|-------|--------|------|-------------|
| `id` | `id` | `UUID` | PK |
| `startDateTime` | `start_date_time` | `LocalDateTime` | Appointment start time |
| `endDateTime` | `end_date_time` | `LocalDateTime` | Appointment end time |
| `status` | `status` | `ReservationStatus` (enum) | Current state of reservation |
| `createdAt` | `created_at` | `LocalDateTime` | When the reservation was made |

**Relationships**:
- `@ManyToOne` -> `Business business` (FK: `business_id`, not null)
- `@ManyToOne` -> `Offering offering` (FK: `service_id`, not null)
- `@ManyToOne` -> `User user` (FK: `user_id`, not null)

---

### `ReservationStatus.java`
**Type**: Enum

| Value | Description |
|-------|-------------|
| `PENDING` | Waiting for business owner approval |
| `CONFIRMED` | Approved and scheduled |
| `CANCELLED` | Cancelled (by customer, owner, or via rejection) |

---

### `ReservationType.java`
**Type**: Enum

| Value | Description |
|-------|-------------|
| `SLOT` | Fixed time slots (e.g., every 30 minutes) |
| `RANGE` | Flexible time ranges |

---

## Sub-package: `entities.notification`

### `Notification.java`
**Table**: `notifications`
**Purpose**: Represents an in-app notification sent to a user.

| Field | Column | Type | Constraints | Description |
|-------|--------|------|-------------|-------------|
| `id` | `notification_id` | `UUID` | PK, unique, not null | Unique identifier |
| `title` | `title` | `String` | Not null | Notification title |
| `message` | `message` | `String` | TEXT column | Full notification message |
| `type` | `type` | `NotificationType` (enum) | Not null | Category of notification |
| `isRead` | `is_read` | `boolean` | Not null, default false | Whether user has read it |
| `targetUrl` | `target_url` | `String` | Optional | URL to navigate to when clicked |
| `createdAt` | `created_at` | `LocalDateTime` | Not null, auto-set | When the notification was created |

**Relationships**:
- `@ManyToOne(fetch = LAZY)` -> `User user` (FK: `user_id`, not null)

**Annotations**: Uses `@Builder` (Lombok) for convenient construction in service layer.

---

### `NotificationType.java`
**Type**: Enum

| Value | Used When |
|-------|-----------|
| `INFO` | General information (e.g., reservation received) |
| `SUCCESS` | Positive outcome (e.g., reservation confirmed) |
| `ALERT` | Important alert (e.g., reservation rejected/cancelled by business) |
| `WARNING` | Warning (e.g., customer cancelled their reservation) |

---

## Sub-package: `entities.review`

### `Review.java`
**Table**: `reviews`
**Purpose**: Represents a customer's review of a business.

| Field | Column | Type | Constraints | Description |
|-------|--------|------|-------------|-------------|
| `id` | `id` | `UUID` | PK | Unique identifier |
| `rating` | `rating` | `Integer` | Not null | Star rating (1-5) |
| `comment` | `comment` | `String` | Max 1000 | Optional review text |
| `createdAt` | `created_at` | `LocalDateTime` | Auto-set | When the review was posted |

**Relationships**:
- `@ManyToOne(fetch = LAZY)` -> `Business business` (FK: `business_id`, not null)
- `@ManyToOne(fetch = LAZY)` -> `User user` (FK: `user_id`, not null)

**Business Rule**: One review per user per business (enforced in `ReviewService`).
