package com.lovemaster.service;

import com.lovemaster.dto.request.CreateMemoryRequest;
import com.lovemaster.dto.request.UpdateMemoryRequest;
import com.lovemaster.dto.response.MemoryResponse;

import java.time.LocalDate;
import java.util.List;

public interface MemoryService {

    MemoryResponse create(Long userId, CreateMemoryRequest request);

    List<MemoryResponse> list(Long userId, String visibility, LocalDate startDate, LocalDate endDate, String tag);

    MemoryResponse get(Long userId, Long memoryId);

    MemoryResponse update(Long userId, Long memoryId, UpdateMemoryRequest request);

    void delete(Long userId, Long memoryId);
}
