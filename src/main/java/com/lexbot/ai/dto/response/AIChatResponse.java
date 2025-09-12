package com.lexbot.ai.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AIChatResponse {

    private List<Choice> choices;

}
