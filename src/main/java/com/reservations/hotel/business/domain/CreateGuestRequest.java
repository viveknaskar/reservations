package com.reservations.hotel.business.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for creating a new guest")
public class CreateGuestRequest {

    @Schema(description = "Guest's first name", example = "Judith")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "Guest's last name", example = "Young")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "Guest's email address — used for lookup", example = "jyoung11@goodreads.com")
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;

    @Schema(description = "Street address", example = "2 Sachtjen Parkway")
    private String address;

    @Schema(description = "Country", example = "United States")
    private String country;

    @Schema(description = "State or region", example = "WV")
    private String state;

    @Schema(description = "Phone number", example = "9-(659)879-6466")
    private String phoneNumber;

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
