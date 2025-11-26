package com.lexbotapp.api.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Role {

    @JsonProperty("user") USER,
    @JsonProperty("developer") DEVELOPER,
    @JsonProperty("assistant") ASSISTANT,
    @JsonProperty("system") SYSTEM

}
