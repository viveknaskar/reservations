package com.reservations.landon.data.repository;

import com.reservations.landon.data.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByNumber(String number);
    List<Room> findByMaxCapacityGreaterThanEqual(int minCapacity);
    List<Room> findByIdNotIn(List<Long> ids);
}
