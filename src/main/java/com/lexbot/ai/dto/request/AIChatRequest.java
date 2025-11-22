package com.lexbot.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIChatRequest {

    private String model;
    private List<AIMessageRequest> messages;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean stream;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer max_tokens;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double temperature;

}
