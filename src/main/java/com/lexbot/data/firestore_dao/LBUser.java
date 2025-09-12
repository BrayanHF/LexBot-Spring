package com.lexbot.data.firestore_dao;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LBUser {

    public static final String PATH = "users";

    @DocumentId
    private String uid;
    private String displayName;
    private String email;
    private LBUserStatus status;

}
