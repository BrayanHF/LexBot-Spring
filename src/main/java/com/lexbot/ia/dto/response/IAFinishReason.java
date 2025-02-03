package com.lexbot.ia.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IAFinishReason {

    @JsonProperty("stop") STOP,
    @JsonProperty("length") LENGTH,
    @JsonProperty("content_filter") CONTENT_FILTER,
    @JsonProperty("function_call") FUNCTION_CALL,
    @JsonProperty("tool_use") TOOL_USE,
    @JsonProperty("tool_calls") TOOL_CALLS,
    @JsonProperty("run_abort") RUN_ABORT,
    @JsonProperty("error") ERROR

}
