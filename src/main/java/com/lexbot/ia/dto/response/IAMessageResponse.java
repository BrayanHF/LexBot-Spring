package com.lexbot.ia.dto.response;

import com.lexbot.ia.dto.Role;
import lombok.Data;

@Data
public class IAMessageResponse {

    private Role role;
    private String content;
    private Refusal refusal;

}
