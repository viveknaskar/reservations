package com.reservations.landon.business.controller;

import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.business.service.ReservationService;
import com.reservations.landon.data.entity.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/reservations")
public class ReservationServiceController {

	@Autowired
	private ReservationService reservationService;

	@GetMapping("/{date}")
	public List<RoomReservation> getAllReservationsForDate(@PathVariable(value = "date") String dateString) {
		return this.reservationService.getRoomReservationsForDate(dateString);
	}

	@GetMapping
	public List<RoomReservation> getAllReservations(@RequestParam(value = "date", required = false) String dateString) {
		return reservationService.getRoomReservationsForDate(dateString);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Reservation createReservation(@RequestBody Reservation reservation) {
		return reservationService.createReservation(reservation);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteReservation(@PathVariable long id) {
		reservationService.deleteReservation(id);
	}

}
