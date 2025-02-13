package com.lexbot.ia.dto.request;

import com.lexbot.ia.dto.IARole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IAMessageRequest {

    private IARole role;
    private String content;

}
