package com.lexbot.ia.dto.request;

import com.lexbot.ia.dto.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IAMessageRequest {

    private Role role;
    private String content;

}
