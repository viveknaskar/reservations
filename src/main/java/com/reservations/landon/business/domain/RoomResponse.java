package com.reservations.landon.business.domain;

import com.reservations.landon.data.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Room details returned by room listing and availability endpoints")
public class RoomResponse {
    @Schema(description = "Room ID", example = "7")
    private long id;
    @Schema(description = "Room wing or room name", example = "Cambridge")
    private String name;
    @Schema(description = "Hotel room number", example = "C2")
    private String number;
    @Schema(description = "Bed configuration code", example = "1K")
    private String bedInfo;
    @Schema(description = "Nightly room price captured for booking calculations", example = "109.00")
    private BigDecimal pricePerNight;
    @Schema(description = "Maximum guest capacity", example = "2")
    private int maxCapacity;

    public static RoomResponse from(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setNumber(room.getNumber());
        response.setBedInfo(room.getBedInfo());
        response.setPricePerNight(room.getPricePerNight());
        response.setMaxCapacity(room.getMaxCapacity());
        return response;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBedInfo() {
        return bedInfo;
    }

    public void setBedInfo(String bedInfo) {
        this.bedInfo = bedInfo;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
