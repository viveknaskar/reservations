package com.reservations.landon.business.service;

import com.reservations.landon.business.domain.CreateReservationRequest;
import com.reservations.landon.business.domain.ReservationResponse;
import com.reservations.landon.data.entity.BookingStatus;
import com.reservations.landon.data.entity.Guest;
import com.reservations.landon.data.entity.Reservation;
import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.GuestRepository;
import com.reservations.landon.data.repository.ReservationRepository;
import com.reservations.landon.data.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceWriteTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservation_savesAndReturnsReservationResponse() {
        Room room = new Room();
        room.setId(1L);
        room.setName("Suite A");
        room.setNumber("1A");
        room.setPricePerNight(new BigDecimal("100.00"));

        Guest guest = new Guest();
        guest.setId(10L);
        guest.setFirstName("John");
        guest.setLastName("Doe");

        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setCheckInDate(LocalDate.of(2024, 6, 15));
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        Reservation saved = new Reservation();
        saved.setId(100L);
        saved.setRoom(room);
        saved.setGuest(guest);
        saved.setCheckInDate(LocalDate.of(2024, 6, 15));
        saved.setCheckOutDate(LocalDate.of(2024, 6, 17));
        saved.setStatus(BookingStatus.PENDING);
        saved.setTotalPrice(new BigDecimal("200.00"));

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(10L)).thenReturn(Optional.of(guest));
        when(reservationRepository.findConflictingReservations(any(), any(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        ReservationResponse result = reservationService.createReservation(request);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getRoomId()).isEqualTo(1L);
        assertThat(result.getGuestId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_invalidDateRange_throwsIllegalArgumentException() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setCheckInDate(LocalDate.of(2024, 6, 17));
        request.setCheckOutDate(LocalDate.of(2024, 6, 15));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Check-out date must be after check-in date");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_roomNotAvailable_throwsIllegalStateException() {
        Room room = new Room();
        room.setId(1L);
        room.setPricePerNight(BigDecimal.TEN);

        Guest guest = new Guest();
        guest.setId(10L);

        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setCheckInDate(LocalDate.of(2024, 6, 15));
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        Reservation existing = new Reservation();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(10L)).thenReturn(Optional.of(guest));
        when(reservationRepository.findConflictingReservations(any(), any(), any(), any()))
            .thenReturn(List.of(existing));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Room is not available for the requested dates");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void deleteReservation_existingId_deletesSuccessfully() {
        when(reservationRepository.existsById(100L)).thenReturn(true);

        reservationService.deleteReservation(100L);

        verify(reservationRepository).deleteById(100L);
    }

    @Test
    void deleteReservation_nonExistingId_throwsEntityNotFoundException() {
        when(reservationRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Reservation not found");

        verify(reservationRepository, never()).deleteById(any());
    }
}
