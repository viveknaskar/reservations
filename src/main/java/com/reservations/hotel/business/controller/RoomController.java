package com.reservations.hotel.business.controller;

import com.reservations.hotel.business.domain.RoomResponse;
import com.reservations.hotel.business.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Rooms", description = "List hotel rooms")
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(
        summary = "List all rooms, or filter by room number",
        description = "Returns RoomResponse DTOs, not JPA entities."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Rooms returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoomResponse.class)))
    )
    @GetMapping
    public List<RoomResponse> findAll(
            @Parameter(description = "Optional exact room number filter", example = "C2")
            @RequestParam(required = false) String roomNumber) {
        return roomService.getAllRooms(roomNumber).stream()
            .map(RoomResponse::from)
            .toList();
    }
}
