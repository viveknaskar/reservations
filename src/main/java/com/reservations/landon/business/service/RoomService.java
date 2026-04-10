package com.reservations.landon.business.service;

import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms(String roomNumber) {
        if (roomNumber == null) {
            return roomRepository.findAll();
        }
        Room room = roomRepository.findByNumber(roomNumber);
        return room != null ? List.of(room) : List.of();
    }
}
