package com.reservations.hotel.data.repository;

import com.reservations.hotel.data.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    List<Guest> findByEmailAddress(String emailAddress);
}