package com.lexbot.chat.dto.generate;

import lombok.Data;

import java.util.Date;

@Data
public abstract class BaseLegalRequest {

    private DocumentId documentId;
    private String fullName;
    private String phone;
    private String email;
    private String city;
    private String address;
    private String date = new Date().toString();

}

