# Package: `com.myapp.reservations.mapper`

This package contains static mapper classes that convert between JPA entities and DTOs (Data Transfer Objects). All mappers use static methods and follow a consistent pattern: `toResponse(Entity)` converts entity to response DTO, and `toEntity(Request)` converts request DTO to entity.

---

## `UserMapper.java`
**Purpose**: Converts between `User` entity and user DTOs.

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toResponse(User)` | `User` entity | `UserResponse` record | Converts `avatarPath` to URL format (`/uploads/{path}`) |
| `toUser(UserRequest)` | `UserRequest` record | `User` entity | Maps name, email, password (plain text), phone. Password hashing is done in the service layer. |

---

## `BusinessMapper.java`
**Purpose**: Converts between `Business` entity and business DTOs. Includes nested mapping of schedule and offerings.

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toResponse(Business)` | `Business` entity | `BusinessResponse` record | Includes: owner ID, admin IDs list, `ScheduleMapper.toResponse()`, offerings mapped via `OfferingMapper.toResponse()`, display category, image URL |
| `toBusiness(BusinessRequest, User, List<User>)` | Request + owner + admins | `Business` entity | Sets all fields from request, assigns owner and admins |
| `toBusiness(BusinessRequest, User)` | Request + owner | `Business` entity | Overload with empty admins list |

**Helper**: `getDisplayCategory(BusinessType, String)` - Converts enum to human-readable name (e.g., `SPA_WELLNESS` -> `"Spa & Wellness"`, `OTHER` -> uses `customType` value).

---

## `ScheduleMapper.java`
**Purpose**: Converts between `ScheduleSettings`/`WorkingDay` entities and their DTOs.

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toScheduleSettings(ScheduleSettingsRequest)` | `ScheduleSettingsRequest` record | `ScheduleSettings` entity | Also maps nested `WorkingDay` list if present |
| `toResponse(ScheduleSettings)` | `ScheduleSettings` entity | `ScheduleSettingsResponse` record | Includes mapped `WorkingDayResponse` list |
| `toWorkingDay(WorkingDayRequest, ScheduleSettings)` | Request + parent settings | `WorkingDay` entity | Private helper; parses `dayOfWeek` string to `DayOfWeek` enum (case-insensitive) |
| `toWorkingDayResponse(WorkingDay)` | `WorkingDay` entity | `WorkingDayResponse` record | Private helper |

---

## `OfferingMapper.java`
**Purpose**: Converts between `Offering` entity and offering DTOs.

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toOffering(OfferingRequest)` | `OfferingRequest` record | `Offering` entity | Maps name, description, price, durationMinutes, bufferTimeMinutes |
| `toResponse(Offering)` | `Offering` entity | `OfferingResponse` record | Includes `businessId` (null-safe extraction from parent business) |

---

## `ReservationMapper.java`
**Purpose**: Converts between `Reservation` entity and reservation DTOs.

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toReservation(ReservationRequest, Business, Offering, User)` | Request + resolved entities | `Reservation` entity | Auto-calculates `endDateTime` from `startTime + offering.durationMinutes`. Sets initial status to `PENDING`. |
| `toResponse(Reservation)` | `Reservation` entity | `ReservationResponse` record | Extracts business name, offering name, user name from related entities |

---

## `NotificationMapper.java`
**Purpose**: Converts `Notification` entity to response DTO. No reverse mapping (notifications are created directly via `Notification.builder()` in the service layer).

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toResponse(Notification)` | `Notification` entity | `NotificationResponse` record | Maps all fields: id, title, message, type, isRead, targetUrl, createdAt |

---

## `TimeOffMapper.java`
**Purpose**: Converts between `TimeOff` entity and time-off DTOs.

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `toTimeOff(TimeOffRequest, ScheduleSettings)` | Request + parent schedule | `TimeOff` entity | Links time-off to schedule settings |
| `toResponse(TimeOff)` | `TimeOff` entity | `TimeOffResponse` record | Maps id, start/end datetimes, reason |

---

## Mapping Flow

```
Controller receives DTO (Request)
        |
        v
Mapper.toEntity(Request) --> Entity
        |
        v
Service processes and saves Entity
        |
        v
Mapper.toResponse(Entity) --> DTO (Response)
        |
        v
Controller returns Response to client
```

**Key patterns**:
- All methods are `static` (no Spring beans, no injection)
- All methods handle `null` input gracefully (return `null`)
- File paths are converted to URL format in `toResponse()` methods (e.g., `avatarPath` -> `/uploads/{avatarPath}`)
- Nested entities are mapped recursively (e.g., `BusinessMapper` calls `ScheduleMapper` and `OfferingMapper`)
