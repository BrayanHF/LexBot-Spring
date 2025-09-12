package com.lexbot.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Choice {

    @JsonAlias({"message", "delta"})
    private AIMessageResponse response;
    private AIFinishReason finish_reason;

}
