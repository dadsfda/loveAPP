package com.lovemaster.dto.response;

import com.lovemaster.entity.Preference;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class PreferenceResponse {

    private Long id;
    private Long creatorId;
    private Long coupleId;
    private String target;
    private String category;
    private String content;
    private String visibility;
    private List<String> tags;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PreferenceResponse fromEntity(Preference preference) {
        PreferenceResponse response = new PreferenceResponse();
        response.setId(preference.getId());
        response.setCreatorId(preference.getCreatorId());
        response.setCoupleId(preference.getCoupleId());
        response.setTarget(preference.getTarget());
        response.setCategory(preference.getCategory());
        response.setContent(preference.getContent());
        response.setVisibility(preference.getVisibility());
        response.setTags(parseTags(preference.getTags()));
        response.setRemark(preference.getRemark());
        response.setCreatedAt(preference.getCreatedAt());
        response.setUpdatedAt(preference.getUpdatedAt());
        return response;
    }

    private static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(tags.split(","))
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toList());
    }
}
