package com.lexbot.ai.dto.response;

import com.lexbot.ai.dto.Role;
import lombok.Data;

@Data
public class AIMessageResponse {

    private Role role;
    private String content;

}
