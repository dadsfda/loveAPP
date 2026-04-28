package com.lovemaster.service;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;

import java.util.List;

public interface AnniversaryService {

    AnniversaryResponse create(Long userId, CreateAnniversaryRequest request);

    List<AnniversaryResponse> list(Long userId, String type, String visibility);

    AnniversaryResponse get(Long userId, Long anniversaryId);

    AnniversaryResponse update(Long userId, Long anniversaryId, UpdateAnniversaryRequest request);

    void delete(Long userId, Long anniversaryId);

    List<AnniversaryResponse> reminders(Long userId, Integer days);
}
