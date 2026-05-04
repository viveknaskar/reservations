package com.reservations.landon.business.service;

import com.reservations.landon.business.domain.CreateReservationRequest;
import com.reservations.landon.business.domain.ReservationResponse;
import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.data.entity.BookingStatus;
import com.reservations.landon.data.entity.Guest;
import com.reservations.landon.data.entity.Reservation;
import com.reservations.landon.data.entity.Room;
import com.reservations.landon.data.repository.GuestRepository;
import com.reservations.landon.data.repository.ReservationRepository;
import com.reservations.landon.data.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(RoomRepository roomRepository, GuestRepository guestRepository,
                               ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        Room room = roomRepository.findByIdForUpdate(request.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        Guest guest = guestRepository.findById(request.getGuestId())
            .orElseThrow(() -> new EntityNotFoundException("Guest not found"));

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate(), BookingStatus.CANCELLED);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Room is not available for the requested dates");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setStatus(BookingStatus.PENDING);
        reservation.setTotalPrice(totalPrice);

        ReservationResponse response = toResponse(reservationRepository.save(reservation));
        log.info("Created reservation id={} room={} guest={} checkIn={} checkOut={} total={}",
            response.getId(), request.getRoomId(), request.getGuestId(),
            request.getCheckInDate(), request.getCheckOutDate(), response.getTotalPrice());
        return response;
    }

    @Transactional(readOnly = true)
    public ReservationResponse getById(long id) {
        return reservationRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
    }

    @Transactional
    public ReservationResponse updateStatus(long id, BookingStatus newStatus) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        validateStatusTransition(reservation.getStatus(), newStatus);
        reservation.setStatus(newStatus);
        ReservationResponse response = toResponse(reservationRepository.save(reservation));
        log.info("Updated reservation id={} status={}", id, newStatus);
        return response;
    }

    @Transactional
    public void deleteReservation(long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        if (reservation.getStatus() != BookingStatus.CANCELLED) {
            reservation.setStatus(BookingStatus.CANCELLED);
            reservationRepository.save(reservation);
        }
        log.info("Cancelled reservation id={}", id);
    }

    @Transactional(readOnly = true)
    public List<RoomReservation> getRoomReservationsForDate(String dateString) {
        return getRoomReservationsForDate(parseDateString(dateString));
    }

    @Transactional(readOnly = true)
    public List<RoomReservation> getRoomReservationsForDate(LocalDate date) {
        Map<Long, RoomReservation> roomReservationMap = new HashMap<>();
        roomRepository.findAll().forEach(room -> {
            RoomReservation rr = new RoomReservation();
            rr.setRoomId(room.getId());
            rr.setRoomName(room.getName());
            rr.setRoomNumber(room.getNumber());
            roomReservationMap.put(room.getId(), rr);
        });
        reservationRepository.findByDateCovering(date, BookingStatus.CANCELLED).forEach(reservation -> {
            RoomReservation rr = roomReservationMap.get(reservation.getRoom().getId());
            if (rr == null) {
                log.warn("Reservation id={} references unknown room id={} — skipping",
                    reservation.getId(), reservation.getRoom().getId());
                return;
            }
            rr.setCheckInDate(reservation.getCheckInDate());
            rr.setCheckOutDate(reservation.getCheckOutDate());
            rr.setFirstName(reservation.getGuest().getFirstName());
            rr.setLastName(reservation.getGuest().getLastName());
            rr.setGuestId(reservation.getGuest().getId());
        });
        return new ArrayList<>(roomReservationMap.values()).stream()
            .sorted(Comparator.comparing(RoomReservation::getRoomNumber))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int minCapacity) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        List<Long> bookedIds = reservationRepository.findBookedRoomIds(checkIn, checkOut, BookingStatus.CANCELLED);
        List<Room> candidates = minCapacity > 1
            ? roomRepository.findByMaxCapacityGreaterThanEqual(minCapacity)
            : roomRepository.findAll();
        if (bookedIds.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
            .filter(r -> !bookedIds.contains(r.getId()))
            .sorted(Comparator.comparing(Room::getNumber))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsForGuest(long guestId) {
        return reservationRepository.findByGuest_Id(guestId).stream()
            .map(this::toResponse)
            .toList();
    }

    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse resp = new ReservationResponse();
        resp.setId(r.getId());
        resp.setRoomId(r.getRoom().getId());
        resp.setRoomName(r.getRoom().getName());
        resp.setRoomNumber(r.getRoom().getNumber());
        resp.setGuestId(r.getGuest().getId());
        resp.setGuestFirstName(r.getGuest().getFirstName());
        resp.setGuestLastName(r.getGuest().getLastName());
        resp.setCheckInDate(r.getCheckInDate());
        resp.setCheckOutDate(r.getCheckOutDate());
        resp.setStatus(r.getStatus());
        resp.setTotalPrice(r.getTotalPrice());
        return resp;
    }

    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }
        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.CANCELLED;
            case CONFIRMED -> newStatus == BookingStatus.CANCELLED;
            case CANCELLED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                "Invalid reservation status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private LocalDate parseDateString(String dateString) {
        if (dateString != null) {
            try {
                return LocalDate.parse(dateString, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                return LocalDate.now();
            }
        }
        return LocalDate.now();
    }
}
