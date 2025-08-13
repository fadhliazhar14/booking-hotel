package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.amenity_type.AmenityTypeRequestDTO;
import com.fadhliazhar.booking_hotel.dto.amenity_type.AmenityTypeResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.model.AmenityType;
import com.fadhliazhar.booking_hotel.repository.AmenityTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AmenityTypeService {
    private final AmenityTypeRepository amenityTypeRepository;

    public List<AmenityTypeResponseDTO> getAll() {
        return amenityTypeRepository.findAll(Sort.by("name")).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AmenityTypeResponseDTO> getAllActive() {
        return amenityTypeRepository.findByIsActiveTrue(Sort.by("name")).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AmenityTypeResponseDTO getById(Long id) {
        AmenityType amenityType = amenityTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity type with ID " + id + " not found."));
        return toResponseDTO(amenityType);
    }

    public AmenityTypeResponseDTO create(AmenityTypeRequestDTO requestDTO) {
        if (amenityTypeRepository.existsByName(requestDTO.getName())) {
            throw new BusinessValidationException("Amenity type with name '" + requestDTO.getName() + "' already exists.");
        }

        AmenityType amenityType = new AmenityType();
        amenityType.setName(requestDTO.getName());
        amenityType.setDescription(requestDTO.getDescription());
        amenityType.setIsActive(requestDTO.getIsActive());

        AmenityType saved = amenityTypeRepository.save(amenityType);
        log.info("Created new amenity type: {}", saved.getName());
        return toResponseDTO(saved);
    }

    public AmenityTypeResponseDTO update(Long id, AmenityTypeRequestDTO requestDTO) {
        AmenityType amenityType = amenityTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity type with ID " + id + " not found."));

        if (!amenityType.getName().equals(requestDTO.getName()) && 
            amenityTypeRepository.existsByName(requestDTO.getName())) {
            throw new BusinessValidationException("Amenity type with name '" + requestDTO.getName() + "' already exists.");
        }

        amenityType.setName(requestDTO.getName());
        amenityType.setDescription(requestDTO.getDescription());
        amenityType.setIsActive(requestDTO.getIsActive());

        AmenityType saved = amenityTypeRepository.save(amenityType);
        log.info("Updated amenity type: {}", saved.getName());
        return toResponseDTO(saved);
    }

    public void deleteById(Long id) {
        if (!amenityTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Amenity type with ID " + id + " not found.");
        }
        amenityTypeRepository.deleteById(id);
        log.info("Deleted amenity type with ID: {}", id);
    }

    private AmenityTypeResponseDTO toResponseDTO(AmenityType amenityType) {
        AmenityTypeResponseDTO dto = new AmenityTypeResponseDTO();
        dto.setId(amenityType.getId());
        dto.setName(amenityType.getName());
        dto.setDescription(amenityType.getDescription());
        dto.setIsActive(amenityType.getIsActive());
        dto.setCreatedOn(amenityType.getCreatedOn());
        dto.setUpdatedOn(amenityType.getUpdatedOn());
        return dto;
    }
}