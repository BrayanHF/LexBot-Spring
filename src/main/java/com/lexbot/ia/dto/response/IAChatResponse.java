package com.lexbot.ia.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class IAChatResponse {

    private List<Choice> choices;
    private Usage usage;

}
