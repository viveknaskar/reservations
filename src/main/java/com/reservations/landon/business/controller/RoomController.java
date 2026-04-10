package com.reservations.landon.business.controller;

import com.reservations.landon.business.service.RoomService;
import com.reservations.landon.data.entity.Room;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> findAll(@RequestParam(required = false) String roomNumber) {
        return roomService.getAllRooms(roomNumber);
    }
}
