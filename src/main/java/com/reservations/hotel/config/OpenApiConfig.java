package com.reservations.hotel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hotelOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Continental Reservations API")
                        .description("REST API for managing hotel rooms, guests, and reservations")
                        .version("1.0.0"));
    }
}
