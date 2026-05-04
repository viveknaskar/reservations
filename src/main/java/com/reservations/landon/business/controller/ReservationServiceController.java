package com.reservations.landon.business.controller;

import com.reservations.landon.business.domain.CreateReservationRequest;
import com.reservations.landon.business.domain.ReservationResponse;
import com.reservations.landon.business.domain.RoomReservation;
import com.reservations.landon.business.service.ReservationService;
import com.reservations.landon.data.entity.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Reservations", description = "Create, retrieve, update, and cancel hotel reservations")
@RestController
@RequestMapping(value = "/api/reservations")
public class ReservationServiceController {

    private final ReservationService reservationService;

    public ReservationServiceController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Get room reservations for a date")
    @GetMapping
    public List<RoomReservation> getReservationsForDate(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return date == null
            ? reservationService.getRoomReservationsForDate((String) null)
            : reservationService.getRoomReservationsForDate(date);
    }

    @Operation(summary = "Get reservation by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservation found"),
        @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable long id) {
        return addLinks(reservationService.getById(id));
    }

    @Operation(summary = "Get all reservations for a guest")
    @GetMapping("/guest/{guestId}")
    public List<ReservationResponse> getReservationsForGuest(@PathVariable long guestId) {
        return reservationService.getReservationsForGuest(guestId).stream()
                .map(this::addLinks)
                .toList();
    }

    @Operation(summary = "Create a new reservation")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reservation created"),
        @ApiResponse(responseCode = "400", description = "Invalid date range or missing fields"),
        @ApiResponse(responseCode = "404", description = "Room or guest not found"),
        @ApiResponse(responseCode = "409", description = "Room already booked for those dates")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return addLinks(reservationService.createReservation(request));
    }

    @Operation(summary = "Update reservation status (PENDING → CONFIRMED or CANCELLED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(
            @PathVariable long id,
            @RequestParam BookingStatus status) {
        return addLinks(reservationService.updateStatus(id, status));
    }

    @Operation(summary = "Delete a reservation")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reservation deleted"),
        @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable long id) {
        reservationService.deleteReservation(id);
    }

    private ReservationResponse addLinks(ReservationResponse response) {
        response.add(linkTo(methodOn(ReservationServiceController.class)
                .getReservationById(response.getId())).withSelfRel());
        response.add(linkTo(methodOn(ReservationServiceController.class)
                .getReservationsForGuest(response.getGuestId())).withRel("guest-reservations"));

        if (response.getStatus() == BookingStatus.PENDING) {
            response.add(linkTo(methodOn(ReservationServiceController.class)
                    .updateStatus(response.getId(), BookingStatus.CONFIRMED)).withRel("confirm"));
            response.add(linkTo(methodOn(ReservationServiceController.class)
                    .updateStatus(response.getId(), BookingStatus.CANCELLED)).withRel("cancel"));
        } else if (response.getStatus() == BookingStatus.CONFIRMED) {
            response.add(linkTo(methodOn(ReservationServiceController.class)
                    .updateStatus(response.getId(), BookingStatus.CANCELLED)).withRel("cancel"));
        }
        return response;
    }
}
