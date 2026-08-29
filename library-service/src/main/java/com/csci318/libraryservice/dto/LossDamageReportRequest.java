package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LossDamageReportRequest(
        @NotNull(message = "Report type is required") ReportType reportType,
        @NotBlank(message = "Customer ID is required") String customerId,
        String notes) {

    public enum ReportType {
        LOST,
        DAMAGED
    }
}
