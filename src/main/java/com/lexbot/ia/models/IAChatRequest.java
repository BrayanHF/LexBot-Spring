package com.lexbot.ia.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IAChatRequest {

    private String model;
    private List<IAMessage> messages;
    private int maxTokens;
    private double temperature;

}
