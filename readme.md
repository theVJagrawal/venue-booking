# Sports Venue Booking Service

A dockerized backend service for managing sports venue bookings with availability checks and concurrency-safe operations.

## Tech Stack

- **Java 17** with Spring Boot 4.0.1
- **MySQL 8.0** for persistence
- **Docker & Docker Compose** for containerization
- **JPA/Hibernate** for ORM
- **Maven** for build management

## Features

- ✅ Venue management (CRUD operations)
- ✅ Time slot management with overlap prevention
- ✅ Venue availability check by sport
- ✅ Booking creation with double-booking prevention
- ✅ Booking cancellation with slot re-availability
- ✅ Pessimistic locking for concurrency safety
- ✅ Sports validation via external API
- ✅ Comprehensive error handling
- ✅ Dockerized application & database


## Quick Start

### Prerequisites

- Docker & Docker Compose installed
- Port 8080 and 3306 available

## 🚀 Running the Application (Docker)

### Start Application
```bash
docker-compose up -d
```

### Stop Application
```bash
docker-compose down
```

### Stop & Clear DB
```bash
docker-compose down -v
```

---

## 🐳 Docker Image (CI/CD)

Image built & published automatically via GitHub Actions.

Image:
thevjagrawal/venue-booking:latest

Run:
```bash
docker run -p 8080:8080 thevjagrawal/venue-booking:latest
```

---


## 🏗️ High-Level Architecture

Client  
→ Spring Boot REST APIs  
→ Service Layer (Business Rules & Validation)  
→ JPA / Hibernate  
→ MySQL Database (Docker)

---

## 🧠 Sports Data Handling

Sports are not hardcoded.

### Source of Truth
External API:
https://stapubox.com/sportslist/

### Approach
- Sports data fetched from public API
- sport_id / sport_code stored in DB
- Venue creation & availability checks rely on DB
- Avoids runtime dependency on external API

---

## Database Schema

### Venues Table
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- name (VARCHAR(255), NOT NULL)
- location (VARCHAR(500), NOT NULL)
- sport_id (VARCHAR(50), NOT NULL, INDEXED)
- sport_name (VARCHAR(255), NOT NULL)
- created_at, updated_at (TIMESTAMP)
```

### Time Slots Table
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- venue_id (BIGINT, FK to venues, INDEXED)
- start_time (DATETIME, NOT NULL)
- end_time (DATETIME, NOT NULL)
- is_available (BOOLEAN, DEFAULT TRUE, INDEXED)
- created_at (TIMESTAMP)
- UNIQUE(venue_id, start_time, end_time)
- CHECK(end_time > start_time)
```

### Bookings Table
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- slot_id (BIGINT, FK to time_slots, INDEXED)
- customer_name (VARCHAR(255), NOT NULL)
- customer_email (VARCHAR(255), NOT NULL, INDEXED)
- customer_phone (VARCHAR(20))
- status (ENUM: CONFIRMED, CANCELLED, INDEXED)
- booking_date (TIMESTAMP)
- cancelled_at (TIMESTAMP, NULL)
- created_at, updated_at (TIMESTAMP)
- INDEX on (slot_id, status) for efficient active booking lookup
```

## API Endpoints

### 1. Create Venue
```bash
POST /venues
Content-Type: application/json

{
  "name": "Central Sports Arena",
  "location": "123 Main St, City",
  "sportId": "CRICKET"
}

Response: 201 Created
{
  "id": 1,
  "name": "Central Sports Arena",
  "location": "123 Main St, City",
  "sportId": "7031809",
  "sportName": "Cricket"
}
```

### 2. Get All Venues
```bash
GET /venues

Response: 200 OK
[
  {
    "id": 1,
    "name": "Central Sports Arena",
    "location": "123 Main St, City",
    "sportId": "7031809",
    "sportName": "Cricket"
  }
]
```

### 3. Get Venue by ID
```bash
GET /venues/{id}

Response: 200 OK
{
  "id": 1,
  "name": "Central Sports Arena",
  "location": "123 Main St, City",
  "sportId": "7031809",
  "sportName": "Cricket"
}
```

### 4. Delete Venue
```bash
DELETE /venues/{id}

Response: 204 No Content
```

### 5. Get Available Venues
```bash
GET /venues/available

Response: 200 OK
[
  {
    "id": 1,
    "name": "Central Sports Arena",
    "location": "123 Main St, City",
    "sportId": "7031809",
    "sportName": "Cricket",
    "availableSlotsCount": 5
  },
  {
    "id": 2,
    "name": "Downtown Football Field",
    "location": "456 Oak Ave",
    "sportId": "7061509",
    "sportName": "Football",
    "availableSlotsCount": 3
  }
]

Note: Returns all venues that have at least one available time slot with the count of available slots
```

### 6. Add Time Slot to Venue
```bash
POST /venues/{venueId}/slots
Content-Type: application/json

{
  "startTime": "2024-12-01T10:00:00",
  "endTime": "2024-12-01T12:00:00"
}

Response: 201 Created
{
  "id": 1,
  "venueId": 1,
  "startTime": "2024-12-01T10:00:00",
  "endTime": "2024-12-01T12:00:00",
  "isAvailable": true
}
```

### 7. Get Slots for Venue
```bash
GET /venues/{venueId}/slots

Response: 200 OK
[
  {
    "id": 1,
    "venueId": 1,
    "startTime": "2024-12-01T10:00:00",
    "endTime": "2024-12-01T12:00:00",
    "isAvailable": true
  }
]
```

### 8. Create Booking
```bash
POST /bookings
Content-Type: application/json

{
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890"
}

Response: 201 Created
{
  "id": 1,
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890",
  "status": "CONFIRMED",
  "bookingDate": "2024-12-01T09:30:00",
  "slot": {
    "id": 1,
    "venueId": 1,
    "startTime": "2024-12-01T10:00:00",
    "endTime": "2024-12-01T12:00:00",
    "isAvailable": false
  }
}
```

### 9. Get All Bookings
```bash
GET /bookings

Response: 200 OK
[
  {
    "id": 1,
    "slotId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "status": "CONFIRMED",
    "bookingDate": "2024-12-01T09:30:00"
  }
]
```

### 10. Get Booking by ID
```bash
GET /bookings/{id}

Response: 200 OK
{
  "id": 1,
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "status": "CONFIRMED"
}
```

### 11. Cancel Booking
```bash
PUT /bookings/{id}/cancel

Response: 200 OK
{
  "id": 1,
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "status": "CANCELLED",
  "slot": {
    "isAvailable": true
  }
}
```

## Project Structure

```
venue-booking/
├── src/
│   ├── main/
│   │   ├── java/com/booking/venuebooking/
│   │   │   ├── VenueBookingApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── VenueController.java
│   │   │   │   ├── TimeSlotController.java
│   │   │   │   └── BookingController.java
│   │   │   ├── service/
│   │   │   │   ├── VenueService.java
│   │   │   │   ├── TimeSlotService.java
│   │   │   │   ├── BookingService.java
│   │   │   │   └── SportsSyncService.java
│   │   │   ├── repository/
│   │   │   │   ├── VenueRepository.java
│   │   │   │   ├── TimeSlotRepository.java
│   │   │   │   ├── BookingRepository.java
│   │   │   │   └── SportRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Venue.java
│   │   │   │   ├── TimeSlot.java
│   │   │   │   ├── Booking.java
│   │   │   │   └── Sport.java
│   │   │   ├── dto/
│   │   │   │   ├── VenueDTO.java
│   │   │   │   ├── CreateVenueRequest.java
│   │   │   │   ├── TimeSlotDTO.java
│   │   │   │   ├── CreateSlotRequest.java
│   │   │   │   ├── BookingDTO.java
│   │   │   │   ├── CreateBookingRequest.java
│   │   │   │   ├── SportDTO.java
│   │   │   │   └── SportsApiResponse.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── BookingException.java
│   │   │       ├── SlotOverlapException.java
│   │   │       └── ErrorResponse.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/booking/venuebooking/
│           └── VenueBookingApplicationTests.java
├── target/
│   └── venue-booking-0.0.1-SNAPSHOT.jar
├── .mvn/
│   └── wrapper/
├── Dockerfile
├── docker-compose.yml
├── init.sql
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Key Assumptions

1. **Booking per Slot**: Each booking occupies exactly one time slot
2. **Immutable Slots**: Once a slot is booked, its time cannot be modified
3. **Immediate Release**: Cancelled bookings free the slot immediately for rebooking
4. **Booking History**: Multiple bookings (including cancelled ones) can exist for same slot to maintain history
5. **Single Database**: Uses single MySQL instance without external caching
6. **Sport Validation**: Sports must exist in external API (https://stapubox.com/sportslist/)
7. **Time Slots**: No overlapping time slots allowed for the same venue
8. **Concurrency**: Uses pessimistic locking (SERIALIZABLE isolation) to prevent race conditions
9. **Time Format**: All timestamps use ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss)
10. **Active Booking Check**: Only CONFIRMED bookings block a slot; CANCELLED bookings allow rebooking

## Concurrency Safety

The system implements multiple layers of protection against double booking:

1. **Database Constraints**: UNIQUE constraint on (venue_id, start_time, end_time)
2. **Pessimistic Locking**: `@Lock(LockModeType.PESSIMISTIC_WRITE)` on slot retrieval
3. **Transaction Isolation**: SERIALIZABLE level for booking creation
4. **Unique Slot Booking**: Database constraint ensures one booking per slot
5. **Atomic Operations**: Slot availability check and booking creation in single transaction

## Error Handling

The API returns appropriate HTTP status codes:

- `200 OK` - Successful GET/PUT operations
- `201 Created` - Successful POST operations
- `204 No Content` - Successful DELETE operations
- `400 Bad Request` - Validation errors, invalid input
- `404 Not Found` - Resource not found
- `409 Conflict` - Time slot overlap
- `500 Internal Server Error` - Unexpected errors


## Database Indexes

For optimal query performance, the following indexes are created:

- `venues.sport_id` - Fast filtering by sport
- `venues.name` - Quick venue name lookups
- `time_slots(venue_id, start_time, end_time)` - Efficient overlap detection
- `time_slots(venue_id, is_available)` - Fast availability queries
- `bookings.customer_email` - Quick customer lookup
- `bookings.status` - Filter by booking status
- `bookings.slot_id` - Fast slot-to-booking joins

## Future Enhancements

- Scheduled job to periodically sync sports data from external API**
  - Runs at fixed intervals (e.g. daily / hourly)
  - Keeps sports data up-to-date without runtime dependency
  - Handles API failures gracefully with retries and fallback
- Multi-venue booking support
- Recurring slot creation
- Email notifications
- Booking history and analytics
- Rate limiting per customer


## License

This project is created for educational purposes.
