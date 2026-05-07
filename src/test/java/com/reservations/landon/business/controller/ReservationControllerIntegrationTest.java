package com.reservations.landon.business.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reservationsPageRendersModernizedUi() throws Exception {
        mockMvc.perform(get("/reservations").param("date", "2017-01-01"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Landon Reservations")))
            .andExpect(content().string(containsString("Room Reservations")))
            .andExpect(content().string(containsString("/css/site.css")))
            .andExpect(content().string(containsString("/js/site.js")))
            .andExpect(content().string(containsString("Booked")))
            .andExpect(content().string(containsString("Vacant")));
    }
}
