package com.lexbotapp.api.ai.dto.response;

import com.lexbotapp.api.ai.dto.Role;
import lombok.Data;

@Data
public class AIMessageResponse {

    private Role role;
    private String content;

}
