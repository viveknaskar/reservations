package com.reservations.hotel.business.controller;

import com.reservations.hotel.business.domain.ApiError;
import com.reservations.hotel.business.domain.CreateGuestRequest;
import com.reservations.hotel.business.domain.GuestResponse;
import com.reservations.hotel.business.service.GuestService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Guests", description = "Create and look up hotel guests")
@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @Operation(summary = "Create a new guest")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Guest created",
            content = @Content(schema = @Schema(implementation = GuestResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Missing or invalid fields",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse createGuest(@Valid @RequestBody CreateGuestRequest request) {
        return guestService.createGuest(request);
    }

    @Operation(summary = "Get guest by ID")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Guest found",
            content = @Content(schema = @Schema(implementation = GuestResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Guest not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping("/{id}")
    public GuestResponse getById(
            @Parameter(description = "Guest ID", example = "85")
            @PathVariable long id) {
        return guestService.getById(id);
    }

    @Operation(
        summary = "Look up guests by email address",
        description = "Returns all guests matching the supplied email. Use this to retrieve a guest ID before creating a reservation."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Matching guests (empty list if none found)",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GuestResponse.class)))
    )
    @GetMapping
    public List<GuestResponse> findByEmail(
            @Parameter(description = "Exact email address to search for", example = "jyoung11@goodreads.com", required = true)
            @RequestParam String email) {
        return guestService.findByEmail(email);
    }
}
