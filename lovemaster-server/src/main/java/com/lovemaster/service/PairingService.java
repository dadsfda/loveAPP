package com.lovemaster.service;

import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.PairingResponse;

public interface PairingService {

    PairingResponse getMyPairing(Long userId);

    PairingResponse bind(Long userId, BindPairRequest request);

    void unbind(Long userId);
}
