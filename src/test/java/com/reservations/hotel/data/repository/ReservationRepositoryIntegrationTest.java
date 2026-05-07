package com.reservations.hotel.data.repository;

import com.reservations.hotel.data.entity.BookingStatus;
import com.reservations.hotel.data.entity.Reservation;
import com.reservations.hotel.data.entity.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationRepositoryIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void findConflictingReservationsFindsOverlappingSeededReservation() {
        Room c2 = roomRepository.findByNumber("C2");

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            c2.getId(),
            LocalDate.of(2017, 1, 2),
            LocalDate.of(2017, 1, 4),
            BookingStatus.CANCELLED);

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getRoom().getNumber()).isEqualTo("C2");
        assertThat(conflicts.get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void findConflictingReservationsTreatsCheckoutAsAvailableBoundary() {
        Room c2 = roomRepository.findByNumber("C2");

        List<Reservation> startsAtCheckout = reservationRepository.findConflictingReservations(
            c2.getId(),
            LocalDate.of(2017, 1, 3),
            LocalDate.of(2017, 1, 5),
            BookingStatus.CANCELLED);
        List<Reservation> endsAtCheckin = reservationRepository.findConflictingReservations(
            c2.getId(),
            LocalDate.of(2016, 12, 30),
            LocalDate.of(2017, 1, 1),
            BookingStatus.CANCELLED);

        assertThat(startsAtCheckout).isEmpty();
        assertThat(endsAtCheckin).isEmpty();
    }

    @Test
    void findBookedRoomIdsIncludesOnlyRoomsOverlappingRange() {
        Room c2 = roomRepository.findByNumber("C2");

        List<Long> overlappingBookedIds = reservationRepository.findBookedRoomIds(
            LocalDate.of(2017, 1, 2),
            LocalDate.of(2017, 1, 4),
            BookingStatus.CANCELLED);
        List<Long> boundaryBookedIds = reservationRepository.findBookedRoomIds(
            LocalDate.of(2017, 1, 3),
            LocalDate.of(2017, 1, 5),
            BookingStatus.CANCELLED);

        assertThat(overlappingBookedIds).containsExactly(c2.getId());
        assertThat(boundaryBookedIds).doesNotContain(c2.getId());
    }

    @Test
    void findByDateCoveringUsesHalfOpenStayDates() {
        List<Reservation> coveredOnStayDate = reservationRepository.findByDateCovering(
            LocalDate.of(2017, 1, 2),
            BookingStatus.CANCELLED);
        List<Reservation> notCoveredOnCheckoutDate = reservationRepository.findByDateCovering(
            LocalDate.of(2017, 1, 3),
            BookingStatus.CANCELLED);

        assertThat(coveredOnStayDate).hasSize(1);
        assertThat(coveredOnStayDate.get(0).getRoom().getNumber()).isEqualTo("C2");
        assertThat(notCoveredOnCheckoutDate).isEmpty();
    }

    @Test
    void cancellingReservationReleasesNightSlotsForFutureBookings() {
        Reservation reservation = reservationRepository.findByDateCovering(
            LocalDate.of(2017, 1, 1),
            BookingStatus.CANCELLED).get(0);

        reservation.setStatus(BookingStatus.CANCELLED);
        reservationRepository.saveAndFlush(reservation);

        List<Long> bookedIds = reservationRepository.findBookedRoomIds(
            LocalDate.of(2017, 1, 1),
            LocalDate.of(2017, 1, 3),
            BookingStatus.CANCELLED);

        assertThat(bookedIds).isEmpty();
    }
}
