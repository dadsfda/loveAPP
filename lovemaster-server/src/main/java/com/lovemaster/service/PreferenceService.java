package com.lovemaster.service;

import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.UpdatePreferenceRequest;
import com.lovemaster.dto.response.PreferenceResponse;

import java.util.List;

public interface PreferenceService {

    PreferenceResponse create(Long userId, CreatePreferenceRequest request);

    List<PreferenceResponse> list(Long userId, String category, String visibility);

    PreferenceResponse get(Long userId, Long preferenceId);

    PreferenceResponse update(Long userId, Long preferenceId, UpdatePreferenceRequest request);

    void delete(Long userId, Long preferenceId);
}
