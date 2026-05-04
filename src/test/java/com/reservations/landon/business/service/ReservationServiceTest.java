package com.reservations.landon.business.service;

import com.reservations.landon.business.domain.ReservationResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    // ── getRoomReservationsForDate ─────────────────────────────────────────────

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
        assertThat(result.get(0).getFirstName()).isNull();
    }

    @Test
    void getRoomReservationsForDate_nullDate_usesToday() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        assertThat(reservationService.getRoomReservationsForDate((String) null)).hasSize(1);
    }

    @Test
    void getRoomReservationsForDate_invalidDateFormat_fallsBackToToday() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        assertThat(reservationService.getRoomReservationsForDate("not-a-date")).hasSize(1);
    }

    @Test
    void getRoomReservationsForDate_noRooms_returnsEmptyList() {
        when(roomRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.findByDateCovering(any(LocalDate.class), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());

        assertThat(reservationService.getRoomReservationsForDate("2024-06-15")).isEmpty();
    }

    // ── findAvailableRooms ────────────────────────────────────────────────────

    @Test
    void findAvailableRooms_noBookedRooms_returnsAllRooms() {
        when(reservationRepository.findBookedRoomIds(any(), any(), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17), 1);

        assertThat(result).containsExactly(room);
        verify(roomRepository).findAll();
        verify(roomRepository, never()).findByMaxCapacityGreaterThanEqual(anyInt());
    }

    @Test
    void findAvailableRooms_someRoomsBooked_filtersThemOut() {
        Room room2 = new Room();
        room2.setId(2L);

        when(reservationRepository.findBookedRoomIds(any(), any(), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of(1L));
        when(roomRepository.findAll()).thenReturn(List.of(room, room2));

        List<Room> result = reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17), 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void findAvailableRooms_withMinCapacity_usesCapacityQuery() {
        when(reservationRepository.findBookedRoomIds(any(), any(), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of());
        when(roomRepository.findByMaxCapacityGreaterThanEqual(3)).thenReturn(List.of(room));

        List<Room> result = reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17), 3);

        assertThat(result).containsExactly(room);
        verify(roomRepository).findByMaxCapacityGreaterThanEqual(3);
        verify(roomRepository, never()).findAll();
    }

    @Test
    void findAvailableRooms_allRoomsBooked_returnsEmptyList() {
        when(reservationRepository.findBookedRoomIds(any(), any(), eq(BookingStatus.CANCELLED)))
            .thenReturn(List.of(1L));
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17), 1);

        assertThat(result).isEmpty();
    }

    @Test
    void findAvailableRooms_invalidDateRange_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 17), LocalDate.of(2024, 6, 15), 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Check-out date must be after check-in date");
    }

    // ── getReservationsForGuest ───────────────────────────────────────────────

    @Test
    void getReservationsForGuest_returnsReservationResponses() {
        when(reservationRepository.findByGuest_Id(10L)).thenReturn(List.of(reservation));

        List<ReservationResponse> result = reservationService.getReservationsForGuest(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGuestId()).isEqualTo(10L);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void getReservationsForGuest_noReservations_returnsEmptyList() {
        when(reservationRepository.findByGuest_Id(99L)).thenReturn(List.of());

        assertThat(reservationService.getReservationsForGuest(99L)).isEmpty();
    }
}
