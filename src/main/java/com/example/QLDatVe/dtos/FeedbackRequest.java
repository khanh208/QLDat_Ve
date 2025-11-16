// src/main/java/com/example/QLDatVe/dtos/FeedbackRequest.java
package com.example.QLDatVe.dtos;

import jakarta.validation.constraints.Max; // For validation
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Trip ID is required")
    private Integer tripId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment; // Comment is optional
}