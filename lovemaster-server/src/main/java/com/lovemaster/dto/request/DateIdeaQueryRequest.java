package com.lovemaster.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DateIdeaQueryRequest {

    private String budgetLevel;

    private String durationLevel;

    private String scene;

    private List<String> tags;
}
