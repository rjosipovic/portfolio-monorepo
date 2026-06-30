package com.studioengine.tutor.catalog;

import com.studioengine.tutor.dataaccess.repositories.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCatalogImpl implements ServiceCatalog {

    private final ServiceCategoryRepository serviceCategoryRepository;

    @Override
    public List<AvailableService> getActiveServices() {
        return serviceCategoryRepository.findByActiveTrue().stream()
                .map(category -> AvailableService.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .price(category.getPrice())
                        .currency(category.getCurrency())
                        .build())
                .toList();
    }
}
