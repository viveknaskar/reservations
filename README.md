# Landon Hotel Reservations

A Spring Boot hotel reservation system inspired by booking.com's core flow. Guests can be looked up, rooms can be searched by availability and capacity, and bookings can be created, confirmed, and cancelled — all through a REST API, backed by an H2 in-memory database with a Thymeleaf web UI.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Booking Flow](#booking-flow)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Configuration](#configuration)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.2 |
| Persistence | Spring Data JPA / Hibernate |
| Database | H2 (in-memory) |
| View Layer | Thymeleaf |
| Build | Maven |
| Testing | JUnit 5, Mockito, Spring MVC Test |
| Coverage | JaCoCo |

---

## Architecture

The application follows a standard layered architecture:

```
src/main/java/com/reservations/landon/
├── business/
│   ├── controller/         # HTTP layer — REST controllers + Thymeleaf controller
│   │   ├── ReservationServiceController.java   # REST: bookings CRUD
│   │   ├── RoomAvailabilityController.java     # REST: room availability search
│   │   ├── RoomController.java                 # REST: room listing
│   │   ├── ReservationController.java          # Web UI (Thymeleaf)
│   │   └── GlobalExceptionHandler.java         # Centralised error responses
│   ├── domain/             # DTOs (request/response objects)
│   │   ├── CreateReservationRequest.java
│   │   ├── ReservationResponse.java
│   │   ├── RoomResponse.java
│   │   └── RoomReservation.java                # View model for the web UI
│   └── service/            # Business logic
│       ├── ReservationService.java
│       └── RoomService.java
└── data/
    ├── entity/             # JPA entities
    │   ├── Room.java
    │   ├── Guest.java
    │   ├── Reservation.java
    │   └── BookingStatus.java   # Enum: PENDING | CONFIRMED | CANCELLED
    └── repository/         # Spring Data repositories
        ├── RoomRepository.java
        ├── GuestRepository.java
        └── ReservationRepository.java
```

**Key design decisions:**

- Controllers never touch repositories directly — all business logic goes through a service.
- Controllers accept and return DTOs (`CreateReservationRequest`, `ReservationResponse`, `RoomResponse`), not JPA entities. This prevents lazy-loading issues and keeps the API contract stable.
- `Reservation` uses `@ManyToOne(fetch = LAZY)` to `Room` and `Guest`, with `spring.jpa.open-in-view=false` enforced. All entity traversal happens inside `@Transactional` service methods.
- `CANCELLED` reservations are never deleted — they are kept for audit history. All availability queries filter them out.
- Reservation creation locks the target room row while checking conflicts and saving, reducing the risk of concurrent double-booking for the same room.

---

## Database Schema

```
ROOM
────────────────────────────────────────────
ROOM_ID          BIGINT  PK  AUTO_INCREMENT
NAME             VARCHAR(16)
ROOM_NUMBER      CHAR(2)     UNIQUE
BED_INFO         CHAR(2)                     e.g. 1K = 1 King, 2D = 2 Double
PRICE_PER_NIGHT  DECIMAL(10,2)
MAX_CAPACITY     INT

GUEST
────────────────────────────────────────────
GUEST_ID         BIGINT  PK  AUTO_INCREMENT
FIRST_NAME       VARCHAR(64)
LAST_NAME        VARCHAR(64)
EMAIL_ADDRESS    VARCHAR(64)
ADDRESS          VARCHAR(64)
COUNTRY          VARCHAR(32)
STATE            VARCHAR(12)
PHONE_NUMBER     VARCHAR(24)

RESERVATION
────────────────────────────────────────────
RESERVATION_ID   BIGINT  PK  AUTO_INCREMENT
ROOM_ID          BIGINT  FK → ROOM
GUEST_ID         BIGINT  FK → GUEST
CHECK_IN_DATE    DATE    NOT NULL
CHECK_OUT_DATE   DATE    NOT NULL
STATUS           VARCHAR(16)   DEFAULT 'PENDING'
TOTAL_PRICE      DECIMAL(10,2) NOT NULL
```

`TOTAL_PRICE` is stored as a snapshot of `pricePerNight × nights` at booking time, so it is unaffected by future room price changes.

Constraints enforce `CHECK_OUT_DATE > CHECK_IN_DATE` and valid status values.

Indexes: `CHECK_IN_DATE`, `CHECK_OUT_DATE`, `GUEST_ID`, `ROOM_ID`, `STATUS`.

The database is seeded on startup from `src/main/resources/data.sql` with 28 rooms across 8 hotel wings and 100+ guests.

---

## Getting Started

**Prerequisites:** Java 17, Maven 3.6+

```bash
# Clone and run
git clone <repo-url>
cd reservations
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

To enable the H2 Console while developing, run with the `dev` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**H2 Console**:
```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (leave blank)
```

**Web UI** (Thymeleaf view showing room bookings by date):
```
http://localhost:8080/reservations?date=2017-01-01
```

---

## API Reference

### Rooms

#### List all rooms
```
GET /rooms
```
Returns all rooms.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Piccadilly",
    "number": "P1",
    "bedInfo": "1Q",
    "pricePerNight": 0.00,
    "maxCapacity": 2
  }
]
```

#### Find room by number
```
GET /rooms?roomNumber=C2
```
Returns a single-element array if found, empty array if not.

---

### Room Availability

#### Search available rooms
```
GET /api/rooms/available?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD&minCapacity=1
```

Returns rooms that have no active (non-cancelled) reservation overlapping the requested date range and meet the minimum capacity requirement.

| Parameter | Required | Default | Description |
|---|---|---|---|
| `checkIn` | Yes | — | Check-in date (`YYYY-MM-DD`) |
| `checkOut` | Yes | — | Check-out date (`YYYY-MM-DD`) |
| `minCapacity` | No | `1` | Minimum guest capacity |

**Example:**
```bash
curl "http://localhost:8080/api/rooms/available?checkIn=2025-07-01&checkOut=2025-07-05&minCapacity=2"
```

**Response:** Array of room response DTOs (same format as `/rooms`).

**Errors:**
- `400 Bad Request` — missing required parameter, invalid date format, `checkOut` ≤ `checkIn`, or `minCapacity` < 1

---

### Reservations

#### Get reservations for a date
```
GET /api/reservations?date=YYYY-MM-DD
```
Returns all rooms with their booking status for the given date. Rooms with no booking on that date are included with guest fields left null. Used to power the Thymeleaf web UI.

#### Get bookings for a guest
```
GET /api/reservations/guest/{guestId}
```
Returns all reservations (across all dates and statuses) for a specific guest.

**Response:**
```json
[
  {
    "id": 1,
    "roomId": 7,
    "roomName": "Cambridge",
    "roomNumber": "C2",
    "guestId": 85,
    "guestFirstName": "Jane",
    "guestLastName": "Young",
    "checkInDate": "2017-01-01",
    "checkOutDate": "2017-01-03",
    "status": "CONFIRMED",
    "totalPrice": 0.00
  }
]
```

#### Create a reservation
```
POST /api/reservations
Content-Type: application/json
```

**Request body:**
```json
{
  "roomId": 7,
  "guestId": 85,
  "checkInDate": "2025-07-01",
  "checkOutDate": "2025-07-05"
}
```

The service will:
1. Validate that `checkOutDate` is after `checkInDate`
2. Verify the room exists
3. Verify the guest exists
4. Check for conflicting reservations (any non-cancelled booking that overlaps the requested range)
5. Calculate `totalPrice = pricePerNight × nights`
6. Save with status `PENDING`

**Response:** `201 Created` with a `ReservationResponse` body.

**Errors:**
- `400 Bad Request` — invalid date range
- `404 Not Found` — room or guest does not exist
- `409 Conflict` — room already booked for those dates

#### Update booking status
```
PATCH /api/reservations/{id}/status?status=CONFIRMED
```

Valid transitions: `PENDING → CONFIRMED`, `PENDING → CANCELLED`, `CONFIRMED → CANCELLED`.

| Status | Meaning |
|---|---|
| `PENDING` | Booking created, awaiting confirmation |
| `CONFIRMED` | Booking confirmed |
| `CANCELLED` | Booking cancelled (soft delete — record kept for audit) |

**Response:** `200 OK` with updated `ReservationResponse`.

**Errors:**
- `400 Bad Request` — invalid status transition
- `404 Not Found` — reservation does not exist

#### Delete a reservation
```
DELETE /api/reservations/{id}
```

Cancels the reservation without deleting the record. This is equivalent to a soft delete and preserves audit history.

**Response:** `204 No Content`

**Errors:**
- `404 Not Found` — reservation does not exist

---

## Booking Flow

```
1. Search available rooms
   GET /api/rooms/available?checkIn=2025-07-01&checkOut=2025-07-05

2. Choose a room (e.g. room ID 7, Cambridge C2)

3. Create a booking
   POST /api/reservations
   { "roomId": 7, "guestId": 85, "checkInDate": "2025-07-01", "checkOutDate": "2025-07-05" }
   → 201 Created, status: PENDING

4. Confirm the booking
   PATCH /api/reservations/1/status?status=CONFIRMED
   → 200 OK, status: CONFIRMED

5. If needed, cancel the booking
   PATCH /api/reservations/1/status?status=CANCELLED
   → 200 OK, status: CANCELLED
   (Room becomes available again for those dates)
```

---

## Error Handling

All errors are handled centrally by `GlobalExceptionHandler` and return a plain-text message body.

| HTTP Status | Trigger |
|---|---|
| `400 Bad Request` | Invalid date range (checkOut ≤ checkIn), missing required query param |
| `404 Not Found` | Room, guest, or reservation not found |
| `409 Conflict` | Room already has an active booking for the requested dates |

---

## Testing

```bash
# Run all tests
mvn test

# Run tests and generate coverage report
mvn verify
open target/site/jacoco/index.html
```

**Current coverage:** 90% instruction / 80% branch across 43 tests.

| Test class | What it covers |
|---|---|
| `ReservationServiceTest` | `getRoomReservationsForDate`, `findAvailableRooms`, `getReservationsForGuest` — all branches |
| `ReservationServiceWriteTest` | `createReservation` (happy path, invalid dates, room not found, guest not found, conflict), `updateStatus`, `deleteReservation` |
| `ReservationServiceControllerTest` | All REST endpoints on `/api/reservations` including 400/404/409 error responses |
| `RoomAvailabilityControllerTest` | `/api/rooms/available` — valid search, missing params, invalid range |
| `RoomControllerTest` | `/rooms` — list all, filter by number, unknown number |

---

## Configuration

`src/main/resources/application.properties`:

```properties
# H2 in-memory database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 web console is disabled by default
spring.h2.console.enabled=false

# Use schema.sql / data.sql for DDL and seed data
spring.jpa.hibernate.ddl-auto=none

# Disable open-in-view to prevent lazy-loading outside transactions
spring.jpa.open-in-view=false
```

To switch to a persistent database (e.g. PostgreSQL), replace the datasource properties and change `ddl-auto` to `validate` or `update`.
