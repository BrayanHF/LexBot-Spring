package com.lexbot.ia.dto.response;

import com.lexbot.ia.dto.IARole;
import lombok.Data;

@Data
public class IAMessageResponse {

    private IARole role;
    private String content;
    private Refusal refusal;

}
