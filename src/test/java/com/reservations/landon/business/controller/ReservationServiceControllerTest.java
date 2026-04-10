package com.reservations.landon.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservations.landon.business.domain.CreateReservationRequest;
import com.reservations.landon.business.domain.ReservationResponse;
import com.reservations.landon.business.service.ReservationService;
import com.reservations.landon.data.entity.BookingStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void createReservation_validBody_returns201WithReservationResponse() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(1L);
        request.setGuestId(10L);
        request.setCheckInDate(LocalDate.of(2024, 6, 15));
        request.setCheckOutDate(LocalDate.of(2024, 6, 17));

        ReservationResponse response = new ReservationResponse();
        response.setId(100L);
        response.setRoomId(1L);
        response.setGuestId(10L);
        response.setStatus(BookingStatus.PENDING);

        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.guestId").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

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
