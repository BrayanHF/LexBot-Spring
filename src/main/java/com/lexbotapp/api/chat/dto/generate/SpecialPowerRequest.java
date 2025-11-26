package com.lexbotapp.api.chat.dto.generate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialPowerRequest extends BaseLegalRequest {

    private DocumentId agentDocumentId;
    private String agentFullName;
    private String agentPhone;
    private String agentEmail;
    private String agentAddress;

    private String grantedPowers;
    private String duration;
    private String attachedDocuments;

}
