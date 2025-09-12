package com.lexbot.chat.dto.generate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ComplaintRequest extends BaseLegalRequest {

    private String authority;
    private String reference;

    private String defendantName;
    private String defendantIdOrDesc;
    private String defendantContact;

    private String conflictDescription;
    private String mainEvidence;
    private String priorNotification;
    private String desiredOutcome;

    private String documents;
    private String otherEvidence;
    private String specialDiligence;

    private String notificationAddress;
    private String defendantNotification;

    private String additionalAttachments;

}
