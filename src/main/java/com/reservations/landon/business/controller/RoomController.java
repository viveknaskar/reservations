package com.reservations.landon.business.controller;

import com.reservations.landon.business.service.RoomService;
import com.reservations.landon.data.entity.Room;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Rooms", description = "List hotel rooms")
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "List all rooms, or filter by room number")
    @GetMapping
    public List<Room> findAll(@RequestParam(required = false) String roomNumber) {
        return roomService.getAllRooms(roomNumber);
    }
}
