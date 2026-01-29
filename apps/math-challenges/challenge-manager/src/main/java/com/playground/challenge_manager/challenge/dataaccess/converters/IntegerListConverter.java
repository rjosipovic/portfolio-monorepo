package com.playground.challenge_manager.challenge.dataaccess.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.challenge_manager.config.StaticContextAccessor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.Objects;

@Converter
public final class IntegerListConverter implements AttributeConverter<List<Integer>, String> {

    private ObjectMapper getObjectMapper() {
        return StaticContextAccessor.getBean(ObjectMapper.class);
    }

    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        try {
            if (Objects.isNull(attribute)) {
                return null;
            }
            return getObjectMapper().writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize list to JSON", e);
        }
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        try {
            if (Objects.isNull(dbData) || dbData.isEmpty()) {
                return null;
            }
            return getObjectMapper().readValue(dbData, new TypeReference<>() {});

        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to list", e);
        }
    }
}
