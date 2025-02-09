# Reservations

A Spring Boot application for managing room reservations with H2 database.

## Features
- RESTful API for reservations and rooms
- Uses Spring Boot, JPA, H2 Database, Thymeleaf
- Provides both web-based UI and API access

## Tech Stack
**Backend**: Java 17, Spring Boot 3.4.x
**Database**: H2 (in-memory)
**View Layer**: Thymeleaf
**Build Tool**: Maven

## API Endpoints

| Endpoint                   | Method | Description                              |
| -------------------------- | ------ | ---------------------------------------- |
| `/api/reservations/{date}` | GET    | Get all reservations for a given date    |
| `/rooms`                   | GET    | Get all rooms (or filter by room number) |

## Database Configuration

By default, the project uses H2 in-memory database:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```


## License

This project is licensed under the MIT License.
