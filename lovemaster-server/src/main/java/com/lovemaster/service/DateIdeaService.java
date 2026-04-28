package com.lovemaster.service;

import com.lovemaster.dto.request.DateIdeaQueryRequest;
import com.lovemaster.dto.response.DateIdeaResponse;

import java.util.List;

public interface DateIdeaService {

    List<DateIdeaResponse> list(DateIdeaQueryRequest request);

    List<DateIdeaResponse> recommend(Long userId, DateIdeaQueryRequest request);
}
