package com.reservations.landon.business.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Request body for creating a hotel reservation")
public class CreateReservationRequest {

    @Schema(description = "Existing room ID", example = "7")
    @Positive(message = "Room ID must be a positive number")
    private long roomId;

    @Schema(description = "Existing guest ID", example = "85")
    @Positive(message = "Guest ID must be a positive number")
    private long guestId;

    @Schema(description = "Number of guests staying in the room", example = "2")
    @Positive(message = "Guest count must be a positive number")
    private int guestCount;

    @Schema(description = "First night of the stay", example = "2025-07-01")
    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @Schema(description = "Checkout date; not occupied as a stay night", example = "2025-07-05")
    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getGuestId() {
        return guestId;
    }

    public void setGuestId(long guestId) {
        this.guestId = guestId;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
