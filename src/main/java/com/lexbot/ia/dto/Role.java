package com.lexbot.ia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Role {

    @JsonProperty("user") USER,
    @JsonProperty("developer") DEVELOPER,
    @JsonProperty("assistant") ASSISTANT

}
