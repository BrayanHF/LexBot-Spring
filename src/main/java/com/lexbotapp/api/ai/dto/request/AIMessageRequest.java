package com.lexbotapp.api.ai.dto.request;

import com.lexbotapp.api.ai.dto.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIMessageRequest {

    private Role role;
    private String content;

}
