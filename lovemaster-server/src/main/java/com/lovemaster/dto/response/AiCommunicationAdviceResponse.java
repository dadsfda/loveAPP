package com.lovemaster.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCommunicationAdviceResponse {

    private String advice;

    private String messageTemplate;

    private String riskLevel;

    private String reminder;

    public AiCommunicationAdviceResponse(String advice, String messageTemplate, String reminder) {
        this.advice = advice;
        this.messageTemplate = messageTemplate;
        this.riskLevel = "low";
        this.reminder = reminder;
    }
}
