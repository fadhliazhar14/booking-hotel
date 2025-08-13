package com.fadhliazhar.booking_hotel.util;

import com.fadhliazhar.booking_hotel.dto.common.PageRequestDTO;
import com.fadhliazhar.booking_hotel.dto.common.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PageUtil {
    
    public static Pageable createPageable(PageRequestDTO pageRequestDTO) {
        Sort.Direction direction = "desc".equalsIgnoreCase(pageRequestDTO.getDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Sort sort = Sort.by(direction, pageRequestDTO.getSort());
        
        return PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize(), sort);
    }
    
    public static <T> PageResponseDTO<T> createPageResponse(Page<T> page, PageRequestDTO pageRequestDTO) {
        return PageResponseDTO.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                pageRequestDTO.getSort(),
                pageRequestDTO.getDirection()
        );
    }
    
    public static <T> PageResponseDTO<T> createPageResponse(List<T> content, Pageable pageable, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        boolean isFirst = pageable.getPageNumber() == 0;
        boolean isLast = pageable.getPageNumber() >= totalPages - 1;
        boolean isEmpty = content.isEmpty();
        
        String sort = pageable.getSort().isSorted() 
                ? pageable.getSort().iterator().next().getProperty()
                : "id";
        String direction = pageable.getSort().isSorted() 
                ? pageable.getSort().iterator().next().getDirection().name().toLowerCase()
                : "asc";
        
        return PageResponseDTO.of(
                content,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                totalElements,
                totalPages,
                isFirst,
                isLast,
                isEmpty,
                sort,
                direction
        );
    }
}