package com.fadhliazhar.booking_hotel.dto.amenity_type;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AmenityTypeResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}