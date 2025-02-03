package com.lexbot.ia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IARole {

    @JsonProperty("user") USER,
    @JsonProperty("system") SYSTEM,
    @JsonProperty("assistant") ASSISTANT

}
