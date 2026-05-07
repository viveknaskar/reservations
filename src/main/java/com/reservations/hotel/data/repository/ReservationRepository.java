package com.reservations.hotel.data.repository;

import com.reservations.hotel.data.entity.BookingStatus;
import com.reservations.hotel.data.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.room.id = :roomId
          AND r.status != :cancelled
          AND r.checkInDate  < :checkOutDate
          AND r.checkOutDate > :checkInDate
        """)
    List<Reservation> findConflictingReservations(
        @Param("roomId")       Long          roomId,
        @Param("checkInDate")  LocalDate     checkInDate,
        @Param("checkOutDate") LocalDate     checkOutDate,
        @Param("cancelled")    BookingStatus cancelled
    );

    @Query("""
        SELECT DISTINCT r.room.id FROM Reservation r
        WHERE r.status != :cancelled
          AND r.checkInDate  < :checkOutDate
          AND r.checkOutDate > :checkInDate
        """)
    List<Long> findBookedRoomIds(
        @Param("checkInDate")  LocalDate     checkInDate,
        @Param("checkOutDate") LocalDate     checkOutDate,
        @Param("cancelled")    BookingStatus cancelled
    );

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.room
        JOIN FETCH r.guest
        WHERE r.checkInDate  <= :date
          AND r.checkOutDate  > :date
          AND r.status != :cancelled
        """)
    List<Reservation> findByDateCovering(
        @Param("date")      LocalDate     date,
        @Param("cancelled") BookingStatus cancelled
    );

    List<Reservation> findByGuest_Id(Long guestId);

    List<Reservation> findByStatus(BookingStatus status);
}
