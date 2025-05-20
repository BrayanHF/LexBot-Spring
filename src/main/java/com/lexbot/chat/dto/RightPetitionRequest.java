package com.lexbot.chat.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RightPetitionRequest {

    private DocumentId documentId;
    private String fullName;
    private String phone;
    private String email;
    private String city;
    private String date = new Date().toString();
    private String address;
    private String recipient;
    private String facts;
    private String request;

}
