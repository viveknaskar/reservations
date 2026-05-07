package com.reservations.landon.business.domain;

import com.reservations.landon.data.entity.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Reservation response with room, guest, status, price, and HATEOAS links")
public class ReservationResponse extends RepresentationModel<ReservationResponse> {
    @Schema(description = "Reservation ID", example = "1")
    private long id;
    @Schema(description = "Booked room ID", example = "7")
    private long roomId;
    @Schema(description = "Booked room name", example = "Cambridge")
    private String roomName;
    @Schema(description = "Booked room number", example = "C2")
    private String roomNumber;
    @Schema(description = "Guest ID", example = "85")
    private long guestId;
    @Schema(description = "Number of guests staying in the room", example = "2")
    private int guestCount;
    @Schema(description = "Guest first name", example = "Judith")
    private String guestFirstName;
    @Schema(description = "Guest last name", example = "Young")
    private String guestLastName;
    @Schema(description = "First night of the stay", example = "2025-07-01")
    private LocalDate checkInDate;
    @Schema(description = "Checkout date; not occupied as a stay night", example = "2025-07-05")
    private LocalDate checkOutDate;
    @Schema(description = "Booking lifecycle status", example = "PENDING")
    private BookingStatus status;
    @Schema(description = "Snapshot total at booking time", example = "436.00")
    private BigDecimal totalPrice;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
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

    public String getGuestFirstName() {
        return guestFirstName;
    }

    public void setGuestFirstName(String guestFirstName) {
        this.guestFirstName = guestFirstName;
    }

    public String getGuestLastName() {
        return guestLastName;
    }

    public void setGuestLastName(String guestLastName) {
        this.guestLastName = guestLastName;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
