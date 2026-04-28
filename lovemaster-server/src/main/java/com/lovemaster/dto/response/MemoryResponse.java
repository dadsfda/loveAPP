package com.lovemaster.dto.response;

import com.lovemaster.entity.Memory;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class MemoryResponse {

    private Long id;
    private Long creatorId;
    private Long coupleId;
    private String title;
    private LocalDate memoryDate;
    private String location;
    private String content;
    private String imageUrl;
    private String visibility;
    private List<String> tags;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemoryResponse fromEntity(Memory memory) {
        MemoryResponse response = new MemoryResponse();
        response.setId(memory.getId());
        response.setCreatorId(memory.getCreatorId());
        response.setCoupleId(memory.getCoupleId());
        response.setTitle(memory.getTitle());
        response.setMemoryDate(memory.getMemoryDate());
        response.setLocation(memory.getLocation());
        response.setContent(memory.getContent());
        response.setImageUrl(memory.getImageUrl());
        response.setVisibility(memory.getVisibility());
        response.setTags(parseTags(memory.getTags()));
        response.setRemark(memory.getRemark());
        response.setCreatedAt(memory.getCreatedAt());
        response.setUpdatedAt(memory.getUpdatedAt());
        return response;
    }

    private static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toList());
    }
}
