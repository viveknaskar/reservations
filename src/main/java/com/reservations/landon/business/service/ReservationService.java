package com.reservations.landon.business.service;

import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.data.entity.Guest;
import com.reservations.landon.data.entity.Reservation;
import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.GuestRepository;
import com.reservations.landon.data.repository.ReservationRepository;
import com.reservations.landon.data.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(RoomRepository roomRepository, GuestRepository guestRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.existsById(id)) {
            throw new EntityNotFoundException("Reservation not found");
        }
        reservationRepository.deleteById(id);
    }

    public List<RoomReservation> getRoomReservationsForDate(String dateString) {
        Date date = this.createDateFromDateString(dateString);
        Map<Long, RoomReservation> roomReservationMap = new HashMap<>();
        this.roomRepository.findAll().forEach(room -> {
            RoomReservation roomReservation = new RoomReservation();
            roomReservation.setRoomId(room.getId());
            roomReservation.setRoomName(room.getName());
            roomReservation.setRoomNumber(room.getNumber());
            roomReservationMap.put(room.getId(), roomReservation);
        });
        List<Reservation> reservations = this.reservationRepository.findByDate(new java.sql.Date(date.getTime()));
        if (!reservations.isEmpty()) {
            List<Long> guestIds = reservations.stream().map(Reservation::getGuestId).collect(Collectors.toList());
            Map<Long, Guest> guestMap = new HashMap<>();
            this.guestRepository.findAllById(guestIds).forEach(g -> guestMap.put(g.getId(), g));
            reservations.forEach(reservation -> {
                Guest guest = guestMap.get(reservation.getGuestId());
                if (guest == null) throw new EntityNotFoundException("Guest not found");
                RoomReservation roomReservation = roomReservationMap.get(reservation.getRoomId());
                roomReservation.setDate(date);
                roomReservation.setFirstName(guest.getFirstName());
                roomReservation.setLastName(guest.getLastName());
                roomReservation.setGuestId(guest.getId());
            });
        }
        return new ArrayList<>(roomReservationMap.values());
    }

    private Date createDateFromDateString(String dateString) {
        if (dateString != null) {
            try {
                LocalDate localDate = LocalDate.parse(dateString, DATE_FORMAT);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException e) {
                return new Date();
            }
        }
        return new Date();
    }
}
