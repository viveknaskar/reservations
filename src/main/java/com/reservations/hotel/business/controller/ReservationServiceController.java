package com.reservations.hotel.business.controller;

import com.reservations.hotel.business.domain.CreateReservationRequest;
import com.reservations.hotel.business.domain.ApiError;
import com.reservations.hotel.business.domain.ReservationResponse;
import com.reservations.hotel.business.domain.RoomReservation;
import com.reservations.hotel.business.service.ReservationService;
import com.reservations.hotel.data.entity.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(
        summary = "Get room reservations for a date",
        description = "Returns one occupancy row per room for the supplied date. Invalid date formats return 400; omitted date uses today."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Room occupancy rows returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoomReservation.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date format",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping
    public List<RoomReservation> getReservationsForDate(
            @Parameter(description = "Date to inspect", example = "2017-01-01")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return date == null
            ? reservationService.getRoomReservationsForDate((String) null)
            : reservationService.getRoomReservationsForDate(date);
    }

    @Operation(summary = "Get reservation by ID")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reservation found",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Reservation not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(
            @Parameter(description = "Reservation ID", example = "1")
            @PathVariable long id) {
        return addLinks(reservationService.getById(id));
    }

    @Operation(summary = "Get all reservations for a guest")
    @ApiResponse(
        responseCode = "200",
        description = "Reservations returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservationResponse.class)))
    )
    @GetMapping("/guest/{guestId}")
    public List<ReservationResponse> getReservationsForGuest(
            @Parameter(description = "Guest ID", example = "85")
            @PathVariable long guestId) {
        return reservationService.getReservationsForGuest(guestId).stream()
                .map(this::addLinks)
                .toList();
    }

    @Operation(
        summary = "Create a new reservation",
        description = "Creates a PENDING reservation and writes one reservation-night slot for each occupied night. The database enforces unique room/date slots, so overlapping active reservations return 409 even under concurrent requests."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Reservation created",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date range, malformed JSON, or missing/invalid request fields",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Room or guest not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Room already booked for those dates. This includes conflicts detected by the reservation-night unique constraint.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return addLinks(reservationService.createReservation(request));
    }

    @Operation(
        summary = "Update reservation status",
        description = "Valid transitions are PENDING to CONFIRMED, PENDING to CANCELLED, and CONFIRMED to CANCELLED. Cancelling releases reservation-night slots."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Status updated",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid status value or invalid status transition",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Reservation not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(
            @Parameter(description = "Reservation ID", example = "1")
            @PathVariable long id,
            @Parameter(description = "Target status", example = "CONFIRMED")
            @RequestParam BookingStatus status) {
        return addLinks(reservationService.updateStatus(id, status));
    }

    @Operation(
        summary = "Cancel a reservation",
        description = "Soft-delete operation: marks the reservation CANCELLED and releases reservation-night slots. The reservation row is retained for audit history."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reservation cancelled; no response body"),
        @ApiResponse(
            responseCode = "404",
            description = "Reservation not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(
            @Parameter(description = "Reservation ID", example = "1")
            @PathVariable long id) {
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
