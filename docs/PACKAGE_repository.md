# Package: `com.myapp.reservations.repository`

This package contains Spring Data JPA repository interfaces that provide the data access layer for all domain entities. Each repository extends `JpaRepository<Entity, UUID>`, inheriting standard CRUD and pagination methods. Custom query methods are defined using either Spring Data JPA derived query naming conventions or explicit `@Query` annotations with JPQL.

---

## `UserRepository.java`
**Entity**: `User`
**Purpose**: Provides data access for user accounts, supporting authentication lookups, uniqueness checks, and role-based queries.

### Custom Query Methods

| Method | Return Type | Parameters | Description |
|--------|-------------|------------|-------------|
| `findByName` | `User` | `String name` | Finds a user by username |
| `findByEmail` | `Optional<User>` | `String email` | Finds a user by email address |
| `existsByEmail` | `boolean` | `String email` | Checks if an email is already registered |
| `existsByName` | `boolean` | `String name` | Checks if a username is already taken |
| `findByRoles` | `Optional<User>` | `Role role` | Finds a user with a specific role |

**Notes**:
- `findByName` returns `User` directly (not Optional), so callers must handle potential `null`
- `existsByEmail` and `existsByName` are used for validation during user registration
- `findByRoles` queries against the `Set<String>` roles collection table

---

## `BusinessRepository.java`
**Entity**: `Business`
**Purpose**: Provides data access for business entities, including lookup by name and retrieval of all businesses owned by a user.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `getBusinessById` | `Optional<Business>` | `UUID uuid` | Derived | Finds a business by its unique ID |
| `getBusinessByName` | `Optional<Business>` | `String name` | Derived | Finds a business by its name |
| `getAllBusinessByUserId` | `List<Business>` | `UUID ownerId` | JPQL (`@Query`) | Retrieves all businesses owned by a specific user |

### JPQL Queries

**`getAllBusinessByUserId`**:
```jpql
SELECT b FROM Business b WHERE b.owner.id = :ownerId
```
- Navigates the `owner` relationship to filter businesses by the owner's user ID
- Parameter is bound via `@Param("ownerId")`

---

## `BusinessPhotoRepository.java`
**Entity**: `BusinessPhoto`
**Annotation**: `@Repository`
**Purpose**: Provides data access for business photo gallery entries, including ordered retrieval, display order management, and count operations.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `findByBusinessIdOrderByDisplayOrderAscCreatedAtDesc` | `List<BusinessPhoto>` | `UUID businessId` | Derived | Gets all photos for a business, sorted by display order (ASC) then creation date (DESC) |
| `findMaxDisplayOrderByBusinessId` | `Integer` | `UUID businessId` | JPQL (`@Query`) | Finds the highest display order value among a business's photos |
| `countByBusinessId` | `long` | `UUID businessId` | Derived | Counts the total number of photos for a business |

### JPQL Queries

**`findMaxDisplayOrderByBusinessId`**:
```jpql
SELECT COALESCE(MAX(p.displayOrder), 0) FROM BusinessPhoto p WHERE p.business.id = :businessId
```
- Returns the maximum `displayOrder` value, or `0` if the business has no photos
- `COALESCE` ensures a non-null default, used when assigning the next display order for a new photo

---

## `ScheduleSettingsRepository.java`
**Entity**: `ScheduleSettings`
**Purpose**: Provides data access for business schedule configuration, including retrieval by business association.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `getScheduleSettingsById` | `ScheduleSettings` | `UUID scheduleId` | Derived | Retrieves schedule settings by primary key |
| `getScheduleSettingsByBusinessId` | `Optional<ScheduleSettings>` | `UUID businessId` | Derived | Retrieves schedule settings for a specific business |
| `findAll` | `List<ScheduleSettings>` | None | Inherited (explicitly declared) | Retrieves all schedule settings |

**Notes**:
- `getScheduleSettingsByBusinessId` traverses the `business` relationship via Spring Data JPA's property expression
- Each business has exactly one `ScheduleSettings` record (one-to-one relationship)

---

## `WorkingDayRepository.java`
**Entity**: `WorkingDay`
**Purpose**: Provides data access for per-day working hours within a schedule configuration.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `findByScheduleSettingsIdAndDayOfWeek` | `WorkingDay` | `UUID scheduleId`, `DayOfWeek dayOfWeek` | Derived | Finds the working day entry for a specific schedule and day of week |

**Notes**:
- Returns `WorkingDay` directly (not Optional), as each schedule has exactly 7 entries (one per day of week)
- Combines two property expressions: `scheduleSettings.id` and `dayOfWeek`

---

## `OfferingRepository.java`
**Entity**: `Offering`
**Purpose**: Provides data access for service offerings (e.g., haircut, massage).

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `getOfferingById` | `Offering` | `UUID offeringId` | Derived | Retrieves an offering by its ID |
| `deleteById` | `void` | `UUID offeringId` | Inherited (explicitly declared) | Deletes an offering by its ID |

**Notes**:
- Both methods mirror inherited `JpaRepository` behavior; they are explicitly declared for clarity
- Offerings are typically loaded via the `Business.offerings` relationship rather than queried directly

---

## `TimeOffRepository.java`
**Entity**: `TimeOff`
**Purpose**: Provides data access for business time-off periods, with range overlap detection and conflict checking used during reservation validation.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `findByScheduleSettingsBusinessId` | `List<TimeOff>` | `UUID businessId` | Derived | Retrieves all time-off periods for a business |
| `findByBusinessIdAndRange` | `List<TimeOff>` | `UUID businessId`, `LocalDateTime viewStart`, `LocalDateTime viewEnd` | JPQL (`@Query`) | Finds time-off periods that overlap with a date range |
| `findOverlappingTimeOff` | `List<TimeOff>` | `UUID businessId`, `LocalDateTime requestedDate` | JPQL (`@Query`) | Finds time-off periods that contain a specific date/time |
| `hasTimeOffConflict` | `boolean` | `UUID businessId`, `LocalDateTime start`, `LocalDateTime end` | JPQL (`@Query`) | Checks if a time range conflicts with any existing time-off period |

### JPQL Queries

**`findByBusinessIdAndRange`**:
```jpql
SELECT t FROM TimeOff t WHERE t.scheduleSettings.business.id = :businessId
AND t.startDateTime < :viewEnd
AND t.endDateTime > :viewStart
```
- Finds time-off periods that overlap with the specified view window
- Used for calendar/schedule views to display unavailable periods

**`findOverlappingTimeOff`**:
```jpql
SELECT t FROM TimeOff t WHERE t.scheduleSettings.business.id = :businessId
AND :requestedDate >= t.startDateTime AND :requestedDate < t.endDateTime
```
- Finds time-off periods that contain the exact requested date/time (inclusive start, exclusive end)
- Used during reservation validation to check if a business is off at the requested time

**`hasTimeOffConflict`**:
```jpql
SELECT COUNT(t) > 0 FROM TimeOff t WHERE t.scheduleSettings.business.id = :businessId
AND :start < t.endDateTime AND :end > t.startDateTime
```
- Returns `true` if any time-off period overlaps with the given range
- More efficient than `findOverlappingTimeOff` when only an existence check is needed

**Notes**:
- All JPQL queries navigate through `scheduleSettings.business.id` to reach the business
- Overlap detection uses the standard interval overlap formula: `start1 < end2 AND end1 > start2`
- `findByScheduleSettingsBusinessId` uses Spring Data JPA's nested property expression to derive the same traversal without explicit JPQL

---

## `ReservationRepository.java`
**Entity**: `Reservation`
**Purpose**: Provides data access for customer reservations, including active-only filtering and overlap detection to prevent double-booking.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `findActiveByBusinessInRange` | `List<Reservation>` | `UUID businessId`, `LocalDateTime viewStart`, `LocalDateTime viewEnd` | JPQL (`@Query`) | Finds non-cancelled reservations overlapping a date range |
| `existsOverlap` | `boolean` | `UUID businessId`, `LocalDateTime start`, `LocalDateTime end` | JPQL (`@Query`) | Checks if a time range overlaps with any active reservation |
| `findByUserId` | `List<Reservation>` | `UUID userId` | Derived | Retrieves all reservations for a specific user |
| `findByBusinessId` | `List<Reservation>` | `UUID businessId` | Derived | Retrieves all reservations for a specific business |

### JPQL Queries

**`findActiveByBusinessInRange`**:
```jpql
SELECT r FROM Reservation r WHERE r.business.id = :businessId
AND r.status != 'CANCELLED'
AND r.endDateTime > :viewStart AND r.startDateTime < :viewEnd
```
- Excludes cancelled reservations from the result set
- Returns reservations that overlap with the specified view window
- Used for calendar views and schedule management

**`existsOverlap`**:
```jpql
SELECT COUNT(r) > 0 FROM Reservation r
WHERE r.business.id = :businessId
AND r.status != 'CANCELLED'
AND :start < r.endDateTime AND :end > r.startDateTime
```
- Returns `true` if any active (non-cancelled) reservation overlaps with the proposed time range
- Used during reservation creation to prevent double-booking

**Notes**:
- "Active" reservations are those with `status != 'CANCELLED'` (includes both `PENDING` and `CONFIRMED`)
- Both JPQL queries use interval overlap logic: `start1 < end2 AND end1 > start2`
- `findByUserId` and `findByBusinessId` return all reservations regardless of status

---

## `NotificationRepository.java`
**Entity**: `Notification`
**Purpose**: Provides data access for in-app notifications, including recent retrieval, unread count, and bulk mark-as-read operations.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `findTop10ByUserIdOrderByCreatedAtDesc` | `List<Notification>` | `UUID userId` | Derived | Retrieves the 10 most recent notifications for a user |
| `countByUserIdAndIsReadFalse` | `long` | `UUID userId` | Derived | Counts unread notifications for a user |
| `markAllAsReadByUserId` | `int` | `UUID userId` | JPQL (`@Query`, `@Modifying`) | Marks all unread notifications as read for a user |

### JPQL Queries

**`markAllAsReadByUserId`**:
```jpql
UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false
```
- Bulk update operation; only affects notifications that are currently unread
- Returns the number of rows updated
- Annotated with `@Modifying` (required for UPDATE/DELETE queries in Spring Data JPA)
- Parameter is bound via `@Param("userId")`

**Notes**:
- `findTop10ByUserIdOrderByCreatedAtDesc` uses Spring Data JPA's `Top` keyword to limit results to 10
- `countByUserIdAndIsReadFalse` uses the `False` keyword suffix to filter on boolean `isRead = false`
- `markAllAsReadByUserId` must be called within a `@Transactional` context

---

## `ReviewRepository.java`
**Entity**: `Review`
**Purpose**: Provides data access for customer reviews of businesses, with ordering and duplicate prevention.

### Custom Query Methods

| Method | Return Type | Parameters | Query Type | Description |
|--------|-------------|------------|------------|-------------|
| `findByBusinessIdOrderByCreatedAtDesc` | `List<Review>` | `UUID businessId` | Derived | Retrieves all reviews for a business, newest first |
| `existsByBusinessIdAndUserId` | `boolean` | `UUID businessId`, `UUID userId` | Derived | Checks if a user has already reviewed a specific business |

**Notes**:
- `existsByBusinessIdAndUserId` enforces the "one review per user per business" business rule at the repository level
- Reviews are sorted by `createdAt DESC` so the most recent reviews appear first

---

## Summary of Repository Patterns

### Naming Conventions
| Prefix | Meaning | Example |
|--------|---------|---------|
| `find...` | Query that returns entities or collections (may be empty) | `findByEmail` |
| `get...` | Query that returns an entity (assumes it exists) | `getBusinessById` |
| `exists...` | Existence check returning `boolean` | `existsByEmail` |
| `count...` | Returns a count of matching records | `countByBusinessId` |
| `delete...` | Removes matching entities | `deleteById` |

### Common JPQL Patterns
- **Interval Overlap**: `start1 < end2 AND end1 > start2` -- used in `ReservationRepository`, `TimeOffRepository`
- **Existence via Count**: `SELECT COUNT(x) > 0` -- more efficient than loading full entities
- **Null-Safe Aggregation**: `COALESCE(MAX(field), default)` -- prevents null results on empty sets
- **Status Filtering**: `status != 'CANCELLED'` -- excludes soft-cancelled records from active queries
- **Relationship Traversal**: `entity.relationship.id = :param` -- navigates JPA associations in JPQL without explicit joins

### Annotations Used
| Annotation | Purpose |
|------------|---------|
| `@Query` | Defines custom JPQL queries on repository methods |
| `@Param` | Binds method parameters to named JPQL query parameters |
| `@Modifying` | Required for `@Query` methods that execute UPDATE or DELETE statements |
| `@Repository` | Explicitly marks an interface as a Spring repository component (optional when extending `JpaRepository`) |
