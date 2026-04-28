package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lovemaster.dto.request.DateIdeaQueryRequest;
import com.lovemaster.dto.response.DateIdeaResponse;
import com.lovemaster.dto.response.PreferenceResponse;
import com.lovemaster.entity.DateIdea;
import com.lovemaster.mapper.DateIdeaMapper;
import com.lovemaster.service.DateIdeaService;
import com.lovemaster.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DateIdeaServiceImpl implements DateIdeaService {

    private final DateIdeaMapper dateIdeaMapper;
    private final PreferenceService preferenceService;

    @Override
    public List<DateIdeaResponse> list(DateIdeaQueryRequest request) {
        return queryIdeas(request).stream()
                .map(DateIdeaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DateIdeaResponse> recommend(Long userId, DateIdeaQueryRequest request) {
        Set<String> preferenceTags = collectPreferenceTags(userId);
        return queryIdeas(request).stream()
                .map(idea -> DateIdeaResponse.fromEntity(idea, calculateMatchScore(idea, preferenceTags)))
                .sorted(Comparator.comparing(DateIdeaResponse::getMatchScore).reversed()
                        .thenComparing(DateIdeaResponse::getId))
                .collect(Collectors.toList());
    }

    private List<DateIdea> queryIdeas(DateIdeaQueryRequest request) {
        DateIdeaQueryRequest safeRequest = request == null ? new DateIdeaQueryRequest() : request;
        LambdaQueryWrapper<DateIdea> wrapper = new LambdaQueryWrapper<DateIdea>()
                .eq(DateIdea::getEnabled, true);
        if (safeRequest.getBudgetLevel() != null && !safeRequest.getBudgetLevel().isBlank()) {
            wrapper.eq(DateIdea::getBudgetLevel, safeRequest.getBudgetLevel());
        }
        if (safeRequest.getDurationLevel() != null && !safeRequest.getDurationLevel().isBlank()) {
            wrapper.eq(DateIdea::getDurationLevel, safeRequest.getDurationLevel());
        }
        if (safeRequest.getScene() != null && !safeRequest.getScene().isBlank()) {
            wrapper.eq(DateIdea::getScene, safeRequest.getScene());
        }
        wrapper.orderByAsc(DateIdea::getId);

        List<String> queryTags = normalizeTags(safeRequest.getTags());
        return dateIdeaMapper.selectList(wrapper).stream()
                .filter(idea -> matchesTags(idea, queryTags))
                .collect(Collectors.toList());
    }

    private Set<String> collectPreferenceTags(Long userId) {
        return preferenceService.list(userId, null, null).stream()
                .flatMap(preference -> {
                    Stream<String> tagStream = preference.getTags() == null
                            ? Stream.empty()
                            : preference.getTags().stream();
                    return Stream.concat(tagStream, Stream.of(preference.getContent()));
                })
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean matchesTags(DateIdea idea, List<String> queryTags) {
        if (queryTags.isEmpty()) {
            return true;
        }
        Set<String> ideaTags = parseTags(idea.getInterestTags());
        return queryTags.stream().anyMatch(ideaTags::contains);
    }

    private Integer calculateMatchScore(DateIdea idea, Set<String> preferenceTags) {
        if (preferenceTags.isEmpty()) {
            return 0;
        }
        Set<String> ideaTags = parseTags(idea.getInterestTags());
        return (int) preferenceTags.stream()
                .filter(tag -> ideaTags.contains(tag) || idea.getTitle().contains(tag))
                .count();
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private Set<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Set.of();
        }
        return Stream.of(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toSet());
    }
}
