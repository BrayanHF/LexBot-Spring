package com.lexbot.ai.dto.request;

import com.lexbot.ai.dto.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIMessageRequest {

    private Role role;
    private String content;

}
