package com.cinebook.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HoldSeatsRequest {
    @NotNull(message = "Show ID is required")
    private UUID showId;

    @NotEmpty(message = "Seat IDs list cannot be empty")
    private List<UUID> showSeatIds;
}
