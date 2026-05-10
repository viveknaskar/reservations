package com.reservations.hotel.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservations.hotel.business.domain.CreateGuestRequest;
import com.reservations.hotel.business.domain.GuestResponse;
import com.reservations.hotel.business.service.GuestService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuestController.class)
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GuestService guestService;

    // ── POST /api/guests ──────────────────────────────────────────────────────

    @Test
    void createGuest_validRequest_returns201WithGuest() throws Exception {
        CreateGuestRequest request = validRequest();

        GuestResponse response = new GuestResponse();
        response.setId(99L);
        response.setFirstName("Judith");
        response.setLastName("Young");
        response.setEmailAddress("jyoung11@goodreads.com");

        when(guestService.createGuest(any(CreateGuestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.firstName").value("Judith"))
                .andExpect(jsonPath("$.lastName").value("Young"))
                .andExpect(jsonPath("$.emailAddress").value("jyoung11@goodreads.com"));
    }

    @Test
    void createGuest_missingFirstName_returns400() throws Exception {
        CreateGuestRequest request = validRequest();
        request.setFirstName(null);

        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("firstName")));
    }

    @Test
    void createGuest_missingLastName_returns400() throws Exception {
        CreateGuestRequest request = validRequest();
        request.setLastName("");

        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createGuest_missingEmail_returns400() throws Exception {
        CreateGuestRequest request = validRequest();
        request.setEmailAddress(null);

        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("emailAddress")));
    }

    @Test
    void createGuest_invalidEmail_returns400() throws Exception {
        CreateGuestRequest request = validRequest();
        request.setEmailAddress("not-an-email");

        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/guests/{id} ──────────────────────────────────────────────────

    @Test
    void getById_existingGuest_returns200() throws Exception {
        GuestResponse response = new GuestResponse();
        response.setId(85L);
        response.setFirstName("Judith");
        response.setLastName("Young");
        response.setEmailAddress("jyoung11@goodreads.com");

        when(guestService.getById(85L)).thenReturn(response);

        mockMvc.perform(get("/api/guests/85"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(85L))
                .andExpect(jsonPath("$.firstName").value("Judith"));
    }

    @Test
    void getById_unknownGuest_returns404() throws Exception {
        when(guestService.getById(999L)).thenThrow(new EntityNotFoundException("Guest not found"));

        mockMvc.perform(get("/api/guests/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Guest not found"));
    }

    // ── GET /api/guests?email= ────────────────────────────────────────────────

    @Test
    void findByEmail_matchingGuest_returnsNonEmptyList() throws Exception {
        GuestResponse response = new GuestResponse();
        response.setId(85L);
        response.setFirstName("Judith");
        response.setLastName("Young");
        response.setEmailAddress("jyoung11@goodreads.com");

        when(guestService.findByEmail("jyoung11@goodreads.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/guests").param("email", "jyoung11@goodreads.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(85L))
                .andExpect(jsonPath("$[0].emailAddress").value("jyoung11@goodreads.com"));
    }

    @Test
    void findByEmail_noMatch_returnsEmptyList() throws Exception {
        when(guestService.findByEmail("unknown@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/guests").param("email", "unknown@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findByEmail_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isBadRequest());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateGuestRequest validRequest() {
        CreateGuestRequest r = new CreateGuestRequest();
        r.setFirstName("Judith");
        r.setLastName("Young");
        r.setEmailAddress("jyoung11@goodreads.com");
        r.setCountry("United States");
        r.setState("WV");
        return r;
    }
}
