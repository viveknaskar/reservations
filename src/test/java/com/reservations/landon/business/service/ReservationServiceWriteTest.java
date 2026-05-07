package com.reservations.landon.business.service;

import com.reservations.landon.business.domain.CreateReservationRequest;
import com.reservations.landon.business.domain.ReservationResponse;
import com.reservations.landon.data.entity.BookingStatus;
import com.reservations.landon.data.entity.Guest;
import com.reservations.landon.data.entity.Reservation;
import com.reservations.landon.data.entity.ReservationNight;
import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.GuestRepository;
import com.reservations.landon.data.repository.ReservationNightRepository;
import com.reservations.landon.data.repository.ReservationRepository;
import com.reservations.landon.data.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceWriteTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationNightRepository reservationNightRepository;

    @InjectMocks
    private ReservationService reservationService;

    // ── createReservation ─────────────────────────────────────────────────────

    @Test
    void createReservation_savesAndReturnsReservationResponse() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);

        CreateReservationRequest request = buildRequest(1L, 10L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17));

        Reservation saved = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.PENDING, new BigDecimal("200.00"));

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(10L)).thenReturn(Optional.of(guest));
        when(reservationRepository.findConflictingReservations(any(), any(), any(), any())).thenReturn(List.of());
        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenReturn(saved);
        when(reservationNightRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse result = reservationService.createReservation(request);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getRoomId()).isEqualTo(1L);
        assertThat(result.getGuestId()).isEqualTo(10L);
        assertThat(result.getGuestCount()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(reservationRepository).saveAndFlush(any(Reservation.class));
        verify(reservationNightRepository).saveAllAndFlush(argThat(nights -> {
            List<ReservationNight> reservationNights = (List<ReservationNight>) nights;
            return reservationNights.size() == 2
                && reservationNights.get(0).getStayDate().equals(LocalDate.of(2024, 6, 15))
                && reservationNights.get(1).getStayDate().equals(LocalDate.of(2024, 6, 16));
        }));
    }

    @Test
    void createReservation_invalidDateRange_throwsIllegalArgumentException() {
        CreateReservationRequest request = buildRequest(1L, 10L,
            LocalDate.of(2024, 6, 17), LocalDate.of(2024, 6, 15));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Check-out date must be after check-in date");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_sameDates_throwsIllegalArgumentException() {
        CreateReservationRequest request = buildRequest(1L, 10L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 15));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalArgumentException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_roomNotFound_throwsEntityNotFoundException() {
        CreateReservationRequest request = buildRequest(99L, 10L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17));

        when(roomRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Room not found");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_guestNotFound_throwsEntityNotFoundException() {
        Room room = buildRoom(1L, "100.00");

        CreateReservationRequest request = buildRequest(1L, 99L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17));

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Guest not found");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_roomNotAvailable_throwsIllegalStateException() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);

        CreateReservationRequest request = buildRequest(1L, 10L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17));

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(10L)).thenReturn(Optional.of(guest));
        when(reservationRepository.findConflictingReservations(any(), any(), any(), any()))
            .thenReturn(List.of(new Reservation()));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Room is not available for the requested dates");

        verify(reservationRepository, never()).saveAndFlush(any());
        verify(reservationNightRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void createReservation_guestCountExceedsRoomCapacity_throwsIllegalArgumentException() {
        Room room = buildRoom(1L, "100.00");
        room.setMaxCapacity(2);
        Guest guest = buildGuest(10L);

        CreateReservationRequest request = buildRequest(1L, 10L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17));
        request.setGuestCount(3);

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(10L)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Guest count exceeds room capacity");

        verify(reservationRepository, never()).findConflictingReservations(any(), any(), any(), any());
        verify(reservationRepository, never()).saveAndFlush(any());
        verify(reservationNightRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void createReservation_slotConstraintViolation_throwsIllegalStateException() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);

        CreateReservationRequest request = buildRequest(1L, 10L,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17));

        Reservation saved = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.PENDING, new BigDecimal("200.00"));

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(10L)).thenReturn(Optional.of(guest));
        when(reservationRepository.findConflictingReservations(any(), any(), any(), any())).thenReturn(List.of());
        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenReturn(saved);
        when(reservationNightRepository.saveAllAndFlush(anyList()))
            .thenThrow(new DataIntegrityViolationException("duplicate night"));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Room is not available for the requested dates");
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_existingReservation_updatesAndReturnsResponse() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);

        Reservation existing = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.PENDING, new BigDecimal("200.00"));

        Reservation updated = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.CONFIRMED, new BigDecimal("200.00"));

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(existing)).thenReturn(updated);

        ReservationResponse result = reservationService.updateStatus(100L, BookingStatus.CONFIRMED);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void updateStatus_confirmedToCancelled_deletesReservationNightSlots() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);

        Reservation existing = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.CONFIRMED, new BigDecimal("200.00"));

        Reservation updated = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.CANCELLED, new BigDecimal("200.00"));

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(existing)).thenReturn(updated);

        ReservationResponse result = reservationService.updateStatus(100L, BookingStatus.CANCELLED);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(reservationNightRepository).deleteByReservation_Id(100L);
    }

    @Test
    void updateStatus_nonExistingReservation_throwsEntityNotFoundException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateStatus(999L, BookingStatus.CONFIRMED))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Reservation not found");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateStatus_cancelledReservationCannotBeConfirmed() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);
        Reservation existing = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.CANCELLED, new BigDecimal("200.00"));

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reservationService.updateStatus(100L, BookingStatus.CONFIRMED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid reservation status transition from CANCELLED to CONFIRMED");

        verify(reservationRepository, never()).save(any());
    }

    // ── deleteReservation ─────────────────────────────────────────────────────

    @Test
    void deleteReservation_existingId_cancelsWithoutDeleting() {
        Room room = buildRoom(1L, "100.00");
        Guest guest = buildGuest(10L);
        Reservation existing = buildReservation(100L, room, guest,
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17),
            BookingStatus.CONFIRMED, new BigDecimal("200.00"));

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(existing));

        reservationService.deleteReservation(100L);

        assertThat(existing.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(reservationNightRepository).deleteByReservation_Id(100L);
        verify(reservationRepository).save(existing);
        verify(reservationRepository, never()).deleteById(any());
    }

    @Test
    void deleteReservation_nonExistingId_throwsEntityNotFoundException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Reservation not found");

        verify(reservationRepository, never()).deleteById(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Room buildRoom(long id, String price) {
        Room r = new Room();
        r.setId(id);
        r.setName("Suite A");
        r.setNumber("1A");
        r.setMaxCapacity(2);
        r.setPricePerNight(new BigDecimal(price));
        return r;
    }

    private Guest buildGuest(long id) {
        Guest g = new Guest();
        g.setId(id);
        g.setFirstName("John");
        g.setLastName("Doe");
        return g;
    }

    private CreateReservationRequest buildRequest(long roomId, long guestId,
                                                   LocalDate checkIn, LocalDate checkOut) {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setRoomId(roomId);
        req.setGuestId(guestId);
        req.setGuestCount(2);
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);
        return req;
    }

    private Reservation buildReservation(long id, Room room, Guest guest,
                                          LocalDate checkIn, LocalDate checkOut,
                                          BookingStatus status, BigDecimal totalPrice) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setRoom(room);
        r.setGuest(guest);
        r.setGuestCount(2);
        r.setCheckInDate(checkIn);
        r.setCheckOutDate(checkOut);
        r.setStatus(status);
        r.setTotalPrice(totalPrice);
        return r;
    }
}
