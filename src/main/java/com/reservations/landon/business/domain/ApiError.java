package com.reservations.landon.business.domain;

import java.time.LocalDateTime;

public record ApiError(int status, String message, String details, LocalDateTime timestamp) {}
