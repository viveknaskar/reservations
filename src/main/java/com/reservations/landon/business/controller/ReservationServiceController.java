package com.reservations.landon.business.controller;

import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.business.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
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

}
