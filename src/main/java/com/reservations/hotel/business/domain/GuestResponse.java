package com.reservations.hotel.business.domain;

import com.reservations.hotel.data.entity.Guest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Guest details")
public class GuestResponse {

    @Schema(example = "85")
    private long id;
    @Schema(example = "Judith")
    private String firstName;
    @Schema(example = "Young")
    private String lastName;
    @Schema(example = "jyoung11@goodreads.com")
    private String emailAddress;
    @Schema(example = "2 Sachtjen Parkway")
    private String address;
    @Schema(example = "United States")
    private String country;
    @Schema(example = "WV")
    private String state;
    @Schema(example = "9-(659)879-6466")
    private String phoneNumber;

    public static GuestResponse from(Guest guest) {
        GuestResponse r = new GuestResponse();
        r.setId(guest.getId());
        r.setFirstName(guest.getFirstName());
        r.setLastName(guest.getLastName());
        r.setEmailAddress(guest.getEmailAddress());
        r.setAddress(guest.getAddress());
        r.setCountry(guest.getCountry());
        r.setState(guest.getState());
        r.setPhoneNumber(guest.getPhoneNumber());
        return r;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
