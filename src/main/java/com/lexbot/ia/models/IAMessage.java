package com.lexbot.ia.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IAMessage {

    private IARole role;
    private String content;

}
