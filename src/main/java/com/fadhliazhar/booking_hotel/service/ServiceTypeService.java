package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.service_type.ServiceTypeRequestDTO;
import com.fadhliazhar.booking_hotel.dto.service_type.ServiceTypeResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.model.ServiceType;
import com.fadhliazhar.booking_hotel.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ServiceTypeService {
    private final ServiceTypeRepository serviceTypeRepository;

    public List<ServiceTypeResponseDTO> getAll() {
        return serviceTypeRepository.findAll(Sort.by("name")).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ServiceTypeResponseDTO> getAllActive() {
        return serviceTypeRepository.findByIsActiveTrue(Sort.by("name")).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ServiceTypeResponseDTO getById(Long id) {
        ServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service type with ID " + id + " not found."));
        return toResponseDTO(serviceType);
    }

    public ServiceTypeResponseDTO create(ServiceTypeRequestDTO requestDTO) {
        if (serviceTypeRepository.existsByName(requestDTO.getName())) {
            throw new BusinessValidationException("Service type with name '" + requestDTO.getName() + "' already exists.");
        }

        ServiceType serviceType = new ServiceType();
        serviceType.setName(requestDTO.getName());
        serviceType.setDescription(requestDTO.getDescription());
        serviceType.setDefaultPrice(requestDTO.getDefaultPrice());
        serviceType.setIsActive(requestDTO.getIsActive());

        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Created new service type: {}", saved.getName());
        return toResponseDTO(saved);
    }

    public ServiceTypeResponseDTO update(Long id, ServiceTypeRequestDTO requestDTO) {
        ServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service type with ID " + id + " not found."));

        if (!serviceType.getName().equals(requestDTO.getName()) && 
            serviceTypeRepository.existsByName(requestDTO.getName())) {
            throw new BusinessValidationException("Service type with name '" + requestDTO.getName() + "' already exists.");
        }

        serviceType.setName(requestDTO.getName());
        serviceType.setDescription(requestDTO.getDescription());
        serviceType.setDefaultPrice(requestDTO.getDefaultPrice());
        serviceType.setIsActive(requestDTO.getIsActive());

        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Updated service type: {}", saved.getName());
        return toResponseDTO(saved);
    }

    public void deleteById(Long id) {
        if (!serviceTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service type with ID " + id + " not found.");
        }
        serviceTypeRepository.deleteById(id);
        log.info("Deleted service type with ID: {}", id);
    }

    private ServiceTypeResponseDTO toResponseDTO(ServiceType serviceType) {
        ServiceTypeResponseDTO dto = new ServiceTypeResponseDTO();
        dto.setId(serviceType.getId());
        dto.setName(serviceType.getName());
        dto.setDescription(serviceType.getDescription());
        dto.setDefaultPrice(serviceType.getDefaultPrice());
        dto.setIsActive(serviceType.getIsActive());
        dto.setCreatedOn(serviceType.getCreatedOn());
        dto.setUpdatedOn(serviceType.getUpdatedOn());
        return dto;
    }
}