package com.lexbot.ia.dto.response;

import lombok.Data;

@Data
public class Choice {

    private IAMessageResponse message;
    private IAFinishReason finish_reason;

}
