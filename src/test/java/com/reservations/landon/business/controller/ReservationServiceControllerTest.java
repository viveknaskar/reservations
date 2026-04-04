package com.reservations.landon.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservations.landon.business.service.ReservationService;
import com.reservations.landon.data.entity.Reservation;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;

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
    void createReservation_validBody_returns201WithSavedReservation() throws Exception {
        Reservation input = new Reservation();
        input.setRoomId(1L);
        input.setGuestId(10L);
        input.setDate(Date.valueOf("2024-06-15"));

        Reservation saved = new Reservation();
        saved.setId(100L);
        saved.setRoomId(1L);
        saved.setGuestId(10L);
        saved.setDate(Date.valueOf("2024-06-15"));

        when(reservationService.createReservation(any(Reservation.class))).thenReturn(saved);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.guestId").value(10L));
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
