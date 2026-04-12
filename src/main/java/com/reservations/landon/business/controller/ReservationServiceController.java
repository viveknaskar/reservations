package com.reservations.landon.business.controller;

import com.reservations.landon.business.domain.CreateReservationRequest;
import com.reservations.landon.business.domain.ReservationResponse;
import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.business.service.ReservationService;
import com.reservations.landon.data.entity.BookingStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/reservations")
public class ReservationServiceController {

    private final ReservationService reservationService;

    public ReservationServiceController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<RoomReservation> getReservationsForDate(
            @RequestParam(value = "date", required = false) String dateString) {
        return reservationService.getRoomReservationsForDate(dateString);
    }

    @GetMapping("/guest/{guestId}")
    public List<ReservationResponse> getReservationsForGuest(@PathVariable long guestId) {
        return reservationService.getReservationsForGuest(guestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(
            @PathVariable long id,
            @RequestParam BookingStatus status) {
        return reservationService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable long id) {
        reservationService.deleteReservation(id);
    }
}
