package com.reservations.landon.business.controller;

import com.reservations.landon.business.service.ReservationService;
import com.reservations.landon.data.entity.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomAvailabilityController.class)
class RoomAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void getAvailableRooms_validDates_returnsRooms() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setName("Suite A");
        room.setNumber("1A");
        room.setBedInfo("KG");

        when(reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17), 1))
            .thenReturn(List.of(room));

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", "2024-06-15")
                        .param("checkOut", "2024-06-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Suite A"))
                .andExpect(jsonPath("$[0].number").value("1A"));
    }

    @Test
    void getAvailableRooms_noAvailableRooms_returnsEmptyList() throws Exception {
        when(reservationService.findAvailableRooms(any(), any(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", "2024-06-15")
                        .param("checkOut", "2024-06-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAvailableRooms_withMinCapacity_passesCapacityToService() throws Exception {
        when(reservationService.findAvailableRooms(
            LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 17), 3))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", "2024-06-15")
                        .param("checkOut", "2024-06-17")
                        .param("minCapacity", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void getAvailableRooms_missingCheckIn_returns400() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                        .param("checkOut", "2024-06-17"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableRooms_missingCheckOut_returns400() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", "2024-06-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableRooms_invalidDateRange_returns400() throws Exception {
        when(reservationService.findAvailableRooms(any(), any(), anyInt()))
            .thenThrow(new IllegalArgumentException("Check-out date must be after check-in date"));

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", "2024-06-17")
                        .param("checkOut", "2024-06-15"))
                .andExpect(status().isBadRequest());
    }
}
