package com.reservations.landon.business.service;

import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.data.entity.BookingStatus;
import com.reservations.landon.data.entity.Guest;
import com.reservations.landon.data.entity.Reservation;
import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.GuestRepository;
import com.reservations.landon.data.repository.ReservationRepository;
import com.reservations.landon.data.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Room room;
    private Guest guest;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1L);
        room.setName("Suite A");
        room.setNumber("1A");
        room.setBedInfo("KG");

        guest = new Guest();
        guest.setId(10L);
        guest.setFirstName("John");
        guest.setLastName("Doe");

        reservation = new Reservation();
        reservation.setId(100L);
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(LocalDate.of(2024, 6, 15));
        reservation.setCheckOutDate(LocalDate.of(2024, 6, 17));
        reservation.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    void getRoomReservationsForDate_withReservation_returnsPopulatedRoomReservation() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of(reservation));

        List<RoomReservation> result = reservationService.getRoomReservationsForDate("2024-06-15");

        assertThat(result).hasSize(1);
        RoomReservation rr = result.get(0);
        assertThat(rr.getRoomId()).isEqualTo(1L);
        assertThat(rr.getRoomName()).isEqualTo("Suite A");
        assertThat(rr.getRoomNumber()).isEqualTo("1A");
        assertThat(rr.getFirstName()).isEqualTo("John");
        assertThat(rr.getLastName()).isEqualTo("Doe");
        assertThat(rr.getGuestId()).isEqualTo(10L);
    }

    @Test
    void getRoomReservationsForDate_noReservations_returnsRoomsWithoutGuests() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        List<RoomReservation> result = reservationService.getRoomReservationsForDate("2024-06-15");

        assertThat(result).hasSize(1);
        RoomReservation rr = result.get(0);
        assertThat(rr.getRoomId()).isEqualTo(1L);
        assertThat(rr.getFirstName()).isNull();
        assertThat(rr.getLastName()).isNull();
    }

    @Test
    void getRoomReservationsForDate_nullDate_usesToday() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        List<RoomReservation> result = reservationService.getRoomReservationsForDate(null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getRoomReservationsForDate_invalidDateFormat_fallsBackToToday() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        List<RoomReservation> result = reservationService.getRoomReservationsForDate("not-a-date");

        assertThat(result).hasSize(1);
    }

    @Test
    void getRoomReservationsForDate_noRooms_returnsEmptyList() {
        when(roomRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        List<RoomReservation> result = reservationService.getRoomReservationsForDate("2024-06-15");

        assertThat(result).isEmpty();
    }
}
