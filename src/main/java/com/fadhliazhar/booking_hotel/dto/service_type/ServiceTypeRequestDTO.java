package com.fadhliazhar.booking_hotel.dto.service_type;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceTypeRequestDTO {
    @NotNull(message = "Name is required")
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Default price must be non-negative")
    private BigDecimal defaultPrice;

    private Boolean isActive = true;
}