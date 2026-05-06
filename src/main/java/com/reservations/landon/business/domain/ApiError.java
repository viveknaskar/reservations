package com.reservations.landon.business.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard API error response")
public record ApiError(int status, String message, String details, LocalDateTime timestamp) {}
