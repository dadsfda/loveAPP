package com.lovemaster.dto.response;

import com.lovemaster.entity.DateIdea;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class DateIdeaResponse {

    private Long id;
    private String title;
    private String budgetLevel;
    private String durationLevel;
    private String scene;
    private List<String> interestTags;
    private List<String> steps;
    private String tips;
    private Integer matchScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DateIdeaResponse fromEntity(DateIdea dateIdea) {
        return fromEntity(dateIdea, 0);
    }

    public static DateIdeaResponse fromEntity(DateIdea dateIdea, Integer matchScore) {
        DateIdeaResponse response = new DateIdeaResponse();
        response.setId(dateIdea.getId());
        response.setTitle(dateIdea.getTitle());
        response.setBudgetLevel(dateIdea.getBudgetLevel());
        response.setDurationLevel(dateIdea.getDurationLevel());
        response.setScene(dateIdea.getScene());
        response.setInterestTags(parseCommaValues(dateIdea.getInterestTags()));
        response.setSteps(parseSemicolonValues(dateIdea.getSteps()));
        response.setTips(dateIdea.getTips());
        response.setMatchScore(matchScore);
        response.setCreatedAt(dateIdea.getCreatedAt());
        response.setUpdatedAt(dateIdea.getUpdatedAt());
        return response;
    }

    private static List<String> parseCommaValues(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }

    private static List<String> parseSemicolonValues(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(value.split(";"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }
}
