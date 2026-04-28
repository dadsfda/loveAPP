package com.lovemaster.service;

import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;

public interface AiAdviceService {

    AiCommunicationAdviceResponse generate(AiCommunicationAdviceRequest request);
}
