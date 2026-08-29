package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryAdjustmentRequest(
        @NotNull(message = "Delta is required") Integer delta,
        @NotBlank(message = "Reason code is required") String reasonCode,
        @NotBlank(message = "Operator ID is required") String operatorId) {
}
