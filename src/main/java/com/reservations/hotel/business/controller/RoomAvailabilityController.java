package com.reservations.hotel.business.controller;

import com.reservations.hotel.business.domain.RoomResponse;
import com.reservations.hotel.business.service.ReservationService;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Room Availability", description = "Search available rooms by date range and capacity")
@Validated
@RestController
@RequestMapping("/api/rooms")
public class RoomAvailabilityController {

    private final ReservationService reservationService;

    public RoomAvailabilityController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(
        summary = "Find available rooms for a date range and minimum capacity",
        description = "Returns rooms with no active reservation-night slots overlapping the requested half-open date range. The checkout date is not occupied."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List of available RoomResponse DTOs",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoomResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Missing parameter, invalid date format, checkOut not after checkIn, or minCapacity less than 1"
        )
    })
    @GetMapping("/available")
    public List<RoomResponse> getAvailableRooms(
            @Parameter(description = "First night of the requested stay", example = "2025-07-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @Parameter(description = "Checkout date; not occupied as a stay night", example = "2025-07-05", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @Parameter(description = "Minimum room capacity", example = "2")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Minimum capacity must be at least 1") int minCapacity) {
        return reservationService.findAvailableRooms(checkIn, checkOut, minCapacity).stream()
            .map(RoomResponse::from)
            .toList();
    }
}
