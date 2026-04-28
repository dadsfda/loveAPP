package com.lovemaster.ai;

import lombok.Data;

import java.util.List;

@Data
public class QwenChatResponse {

    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
