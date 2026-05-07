package com.reservations.hotel.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservations.hotel.business.domain.CreateReservationRequest;
import com.reservations.hotel.business.domain.ReservationResponse;
import com.reservations.hotel.business.domain.RoomReservation;
import com.reservations.hotel.business.service.ReservationService;
import com.reservations.hotel.data.entity.BookingStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import org.hamcrest.Matchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationServiceController.class)
class ReservationServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    // ── GET /api/reservations ─────────────────────────────────────────────────

    @Test
    void getReservationsForDate_returns200WithList() throws Exception {
        RoomReservation rr = new RoomReservation();
        rr.setRoomId(1L);
        rr.setRoomName("Suite A");

        when(reservationService.getRoomReservationsForDate(LocalDate.of(2024, 6, 15))).thenReturn(List.of(rr));

        mockMvc.perform(get("/api/reservations").param("date", "2024-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value(1L))
                .andExpect(jsonPath("$[0].roomName").value("Suite A"));
    }

    @Test
    void getReservationsForDate_noParam_returns200() throws Exception {
        when(reservationService.getRoomReservationsForDate((String) null)).thenReturn(List.of());

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk());
    }

    // ── GET /api/reservations/guest/{guestId} ─────────────────────────────────

    @Test
    void getReservationsForGuest_returns200WithList() throws Exception {
        ReservationResponse resp = new ReservationResponse();
        resp.setId(100L);
        resp.setGuestId(10L);
        resp.setStatus(BookingStatus.CONFIRMED);

        when(reservationService.getReservationsForGuest(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/reservations/guest/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestId").value(10L))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void getReservationsForGuest_noReservations_returnsEmptyList() throws Exception {
        when(reservationService.getReservationsForGuest(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/reservations/guest/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /api/reservations ────────────────────────────────────────────────

    @Test
    void createReservation_validBody_returns201WithReservationResponse() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setGuestCount(2);
        request.setCheckInDate(LocalDate.of(2024, 6, 15));
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        ReservationResponse response = new ReservationResponse();
        response.setId(100L);
        response.setRoomId(1L);
        response.setGuestId(10L);
        response.setGuestCount(2);
        response.setStatus(BookingStatus.PENDING);

        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.guestId").value(10L))
                .andExpect(jsonPath("$.guestCount").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReservation_nullCheckInDate_returns400WithValidationMessage() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setGuestCount(2);
        // checkInDate intentionally left null
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("checkInDate")));
    }

    @Test
    void createReservation_invalidDateRange_returns400() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setGuestCount(2);
        request.setCheckInDate(LocalDate.of(2024, 6, 17));
        request.setCheckOutDate(LocalDate.of(2024, 6, 15));

        when(reservationService.createReservation(any()))
            .thenThrow(new IllegalArgumentException("Check-out date must be after check-in date"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReservation_roomNotAvailable_returns409() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setGuestCount(2);
        request.setCheckInDate(LocalDate.of(2024, 6, 15));
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        when(reservationService.createReservation(any()))
            .thenThrow(new IllegalStateException("Room is not available for the requested dates"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createReservation_roomNotFound_returns404() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(99L);
        request.setGuestId(10L);
        request.setGuestCount(2);
        request.setCheckInDate(LocalDate.of(2024, 6, 15));
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        when(reservationService.createReservation(any()))
            .thenThrow(new EntityNotFoundException("Room not found"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReservationsForDate_invalidDate_returns400() throws Exception {
        mockMvc.perform(get("/api/reservations").param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /api/reservations/{id}/status ───────────────────────────────────

    @Test
    void updateStatus_returns200WithUpdatedResponse() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(100L);
        response.setStatus(BookingStatus.CONFIRMED);

        when(reservationService.updateStatus(100L, BookingStatus.CONFIRMED)).thenReturn(response);

        mockMvc.perform(patch("/api/reservations/100/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_reservationNotFound_returns404() throws Exception {
        when(reservationService.updateStatus(eq(999L), any()))
            .thenThrow(new EntityNotFoundException("Reservation not found"));

        mockMvc.perform(patch("/api/reservations/999/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/reservations/{id} ─────────────────────────────────────────

    @Test
    void deleteReservation_existingId_returns204() throws Exception {
        doNothing().when(reservationService).deleteReservation(100L);

        mockMvc.perform(delete("/api/reservations/100"))
                .andExpect(status().isNoContent());

        verify(reservationService).deleteReservation(100L);
    }

    @Test
    void deleteReservation_nonExistingId_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Reservation not found"))
                .when(reservationService).deleteReservation(999L);

        mockMvc.perform(delete("/api/reservations/999"))
                .andExpect(status().isNotFound());
    }
}
