package com.lovemaster.dto.response;

import com.lovemaster.entity.Anniversary;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class AnniversaryResponse {

    private Long id;
    private Long creatorId;
    private Long coupleId;
    private String title;
    private LocalDate date;
    private String type;
    private String visibility;
    private List<Integer> remindDays;
    private Boolean surpriseMode;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnniversaryResponse fromEntity(Anniversary anniversary) {
        AnniversaryResponse response = new AnniversaryResponse();
        response.setId(anniversary.getId());
        response.setCreatorId(anniversary.getCreatorId());
        response.setCoupleId(anniversary.getCoupleId());
        response.setTitle(anniversary.getTitle());
        response.setDate(anniversary.getDate());
        response.setType(anniversary.getType());
        response.setVisibility(anniversary.getVisibility());
        response.setRemindDays(parseRemindDays(anniversary.getRemindDays()));
        response.setSurpriseMode(Boolean.TRUE.equals(anniversary.getSurpriseMode()));
        response.setRemark(anniversary.getRemark());
        response.setCreatedAt(anniversary.getCreatedAt());
        response.setUpdatedAt(anniversary.getUpdatedAt());
        return response;
    }

    private static List<Integer> parseRemindDays(String remindDays) {
        if (remindDays == null || remindDays.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(remindDays.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
