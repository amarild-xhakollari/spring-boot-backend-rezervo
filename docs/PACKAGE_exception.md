# Package: `com.myapp.reservations.exception`

This package contains the exception hierarchy, the global exception handler, and the structured error response used throughout the application. Exceptions are organized into sub-packages by category.

---

## `ErrorResponse.java`
**Purpose**: Structured JSON error response returned to the client when an exception occurs.

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | `Instant` | Auto-set to `Instant.now()` at construction |
| `status` | `int` | HTTP status code (e.g., 400, 404, 409) |
| `error` | `String` | Error message describing what went wrong |
| `path` | `String` | The request URI that caused the error |

**Example JSON response**:
```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 404,
  "error": "Business not found with ID : 123e4567-...",
  "path": "/api/businesses/123e4567-..."
}
```

---

## `GlobalExceptionHandler.java`
**Type**: `@RestControllerAdvice`
**Purpose**: Catches all exceptions thrown by controllers/services and maps them to appropriate HTTP responses with `ErrorResponse` bodies.

| Exception Type | HTTP Status | Code | When Thrown |
|----------------|-------------|------|-------------|
| `IllegalArgumentException` | `400 Bad Request` | 400 | Invalid input parameters |
| `UsernameNotFoundException` | `401 Unauthorized` | 401 | Spring Security auth failure |
| `UnauthorizedException` | `403 Forbidden` | 403 | User lacks permission for the action |
| `NotFoundException` | `404 Not Found` | 404 | Requested resource does not exist |
| `ConflictException` | `409 Conflict` | 409 | Resource conflict (duplicates, limits) |
| `BusinessRuleViolationException` | `422 Unprocessable Entity` | 422 | Business logic rule violated |
| `RuntimeException` (catch-all) | `500 Internal Server Error` | 500 | Unexpected/unhandled errors |

---

## Base Exception Classes

### `UnauthorizedException.java`
**Extends**: `RuntimeException` | **HTTP**: `403 Forbidden`

| Constructor | Message Pattern |
|-------------|-----------------|
| `(String resourceType, String action)` | `"Not authorized to {action} this {resourceType}"` |
| `(String message)` | Custom message |

### `ConflictException.java`
**Extends**: `RuntimeException` | **HTTP**: `409 Conflict`

Base class for all conflict-related exceptions.

### `BusinessRuleViolationException.java`
**Extends**: `RuntimeException` | **HTTP**: `422 Unprocessable Entity`

Base class for all business rule violation exceptions.

### `NotFoundException.java` (in `notfoundexceptions` sub-package)
**Extends**: `RuntimeException` | **HTTP**: `404 Not Found`

Base class for all not-found exceptions. Constructor is `protected` to force usage of specific subclasses.

---

## Sub-package: `notfoundexceptions`

All extend `RuntimeException` and follow the same pattern: accept a `UUID` (by ID) and/or `String` (by name) constructor.

| Exception Class | Message Pattern |
|-----------------|-----------------|
| `UserNotFoundException` | `"User not found with ID : {id}"` or `"...with name : {name}"` |
| `BusinessNotFoundException` | `"Business not found with ID : {id}"` or `"...with name : {name}"` |
| `BusinessPhotoNotFoundException` | Photo not found by ID |
| `OfferingNotFoundException` | Offering not found by ID |
| `ReservationNotFoundException` | Reservation not found by ID |
| `ReviewNotFoundException` | Review not found by ID |
| `NotificationNotFoundException` | Notification not found by ID |
| `ScheduleNotFoundException` | Schedule not found by ID |
| `ScheduleSettingsNotFoundException` | Schedule settings not found by ID |
| `TimeOffNotFoundException` | Time-off not found by ID |
| `WorkingDayNotFoundException` | Working day not found by ID |
| `AvailabilityNotFoundException` | Availability data not found |

---

## Sub-package: `conflictexceptions`

All extend `ConflictException` (-> `409 Conflict`).

| Exception Class | Default Message | Thrown When |
|-----------------|-----------------|-------------|
| `DuplicateReviewException` | `"You have already reviewed this business"` | User tries to review same business twice |
| `MaxPhotosReachedException` | `"Maximum number of photos ({max}) reached"` | Business gallery exceeds photo limit (20) |
| `ReservationConflictException` | `"This time slot is already reserved by another customer"` | Overlapping reservation detected |

---

## Sub-package: `businessruleviolations`

All extend `BusinessRuleViolationException` (-> `422 Unprocessable Entity`). These represent reservation validation failures.

| Exception Class | Default Message | Thrown When |
|-----------------|-----------------|-------------|
| `PastDateReservationException` | `"Cannot create a reservation for a past date"` | Reservation start time is in the past |
| `BookingLeadTimeException` | `"...Minimum lead time is {hours} hours"` | Too short notice (violates `minAdvanceBookingHours`) |
| `BookingWindowException` | `"...You can only book up to {days} days in advance"` | Too far ahead (violates `maxAdvanceBookingDays`) |
| `BusinessClosedException` | `"The business is closed on {dayOfWeek}"` | Reservation on a day-off |
| `OutsideWorkingHoursException` | `"Selected time is outside of business working hours"` | Reservation outside open/close times |
| `BreakTimeConflictException` | `"Selected time overlaps with a business break"` | Reservation during break period |
| `BusinessUnavailableException` | `"The business is unavailable during this time (Maintenance/Time Off)"` | Reservation during time-off period |
| `InvalidReservationStateException` | `"Cannot {action} reservation in {state} state"` | Invalid state transition (e.g., confirming a cancelled reservation) |

---

## Exception Hierarchy Diagram

```
RuntimeException
├── UnauthorizedException                    -> 403
├── ConflictException                        -> 409
│   ├── DuplicateReviewException
│   ├── MaxPhotosReachedException
│   └── ReservationConflictException
├── BusinessRuleViolationException           -> 422
│   ├── PastDateReservationException
│   ├── BookingLeadTimeException
│   ├── BookingWindowException
│   ├── BusinessClosedException
│   ├── OutsideWorkingHoursException
│   ├── BreakTimeConflictException
│   ├── BusinessUnavailableException
│   └── InvalidReservationStateException
└── NotFoundException                        -> 404
    ├── UserNotFoundException
    ├── BusinessNotFoundException
    ├── BusinessPhotoNotFoundException
    ├── OfferingNotFoundException
    ├── ReservationNotFoundException
    ├── ReviewNotFoundException
    ├── NotificationNotFoundException
    ├── ScheduleNotFoundException
    ├── ScheduleSettingsNotFoundException
    ├── TimeOffNotFoundException
    ├── WorkingDayNotFoundException
    └── AvailabilityNotFoundException
```
