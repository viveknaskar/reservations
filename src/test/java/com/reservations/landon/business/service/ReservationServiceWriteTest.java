package com.reservations.landon.business.service;

import com.reservations.landon.data.entity.Reservation;
import com.reservations.landon.data.repository.GuestRepository;
import com.reservations.landon.data.repository.ReservationRepository;
import com.reservations.landon.data.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void createReservation_savesAndReturnsReservation() {
        Reservation input = new Reservation();
        input.setRoomId(1L);
        input.setGuestId(10L);
        input.setDate(Date.valueOf("2024-06-15"));

        Reservation saved = new Reservation();
        saved.setId(100L);
        saved.setRoomId(1L);
        saved.setGuestId(10L);
        saved.setDate(Date.valueOf("2024-06-15"));

        when(reservationRepository.save(input)).thenReturn(saved);

        Reservation result = reservationService.createReservation(input);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getRoomId()).isEqualTo(1L);
        assertThat(result.getGuestId()).isEqualTo(10L);
        verify(reservationRepository).save(input);
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
