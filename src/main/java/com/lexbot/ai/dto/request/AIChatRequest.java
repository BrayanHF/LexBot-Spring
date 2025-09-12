package com.lexbot.ai.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIChatRequest {

    private String model;
    private List<AIMessageRequest> messages;
    private boolean stream;
    private int max_tokens;
    private double temperature;

}
