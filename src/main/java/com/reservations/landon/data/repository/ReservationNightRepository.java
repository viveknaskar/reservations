package com.reservations.landon.data.repository;

import com.reservations.landon.data.entity.ReservationNight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationNightRepository extends JpaRepository<ReservationNight, Long> {
    void deleteByReservation_Id(Long reservationId);
}
