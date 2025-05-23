package com.lexbot.chat.dto.generate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RightPetitionRequest extends BaseLegalRequest {

    private String recipient;
    private String facts;
    private String request;

}
