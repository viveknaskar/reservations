package com.reservations.hotel.business.controller;

import com.reservations.hotel.business.service.RoomService;
import com.reservations.hotel.data.entity.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void findAll_noFilter_returnsAllRooms() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setName("Suite A");
        room.setNumber("1A");
        room.setBedInfo("KG");

        when(roomService.getAllRooms(null)).thenReturn(List.of(room));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Suite A"))
                .andExpect(jsonPath("$[0].number").value("1A"));
    }

    @Test
    void findAll_withRoomNumber_returnsMatchingRoom() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setName("Suite A");
        room.setNumber("1A");
        room.setBedInfo("KG");

        when(roomService.getAllRooms("1A")).thenReturn(List.of(room));

        mockMvc.perform(get("/rooms").param("roomNumber", "1A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Suite A"));
    }

    @Test
    void findAll_withUnknownRoomNumber_returnsEmptyList() throws Exception {
        when(roomService.getAllRooms("99")).thenReturn(List.of());

        mockMvc.perform(get("/rooms").param("roomNumber", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
