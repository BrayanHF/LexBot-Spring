package com.lexbotapp.api.data.firestore_dao;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {

    public static final String PATH = "chats";

    @DocumentId
    private String id;
    private String title;
    private Date lastUse;
    private String resume;

}
