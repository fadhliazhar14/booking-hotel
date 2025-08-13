package com.fadhliazhar.booking_hotel.dto.service_type;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceTypeResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal defaultPrice;
    private Boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}