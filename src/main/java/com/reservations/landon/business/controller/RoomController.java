package com.reservations.landon.business.controller;

import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomRepository repository;

    @GetMapping
    List<Room> findAll(@RequestParam(required = false) String roomNumber) {
        if (roomNumber == null) {
            return (List<Room>) repository.findAll();
        } else {
            Room room = this.repository.findByNumber(roomNumber);
            return (room != null) ? List.of(room) : List.of();
        }
    }
}
