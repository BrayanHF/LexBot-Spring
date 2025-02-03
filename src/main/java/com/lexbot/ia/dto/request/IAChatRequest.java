package com.lexbot.ia.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IAChatRequest {

    private String model;
    private List<IAMessageRequest> messages;
    private int max_tokens;
    private double temperature;

}
