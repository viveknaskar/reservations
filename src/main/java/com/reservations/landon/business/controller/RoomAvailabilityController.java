package com.reservations.landon.business.controller;

import com.reservations.landon.business.domain.RoomResponse;
import com.reservations.landon.business.service.ReservationService;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Find available rooms for a date range and minimum capacity")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of available rooms"),
        @ApiResponse(responseCode = "400", description = "Invalid date range or missing parameters")
    })
    @GetMapping("/available")
    public List<RoomResponse> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Minimum capacity must be at least 1") int minCapacity) {
        return reservationService.findAvailableRooms(checkIn, checkOut, minCapacity).stream()
            .map(RoomResponse::from)
            .toList();
    }
}
