package com.lexbotapp.api.chat.dto.generate;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HabeasDataRequest extends BaseLegalRequest {

    private String entityName;
    private String entityLocation;
    private String requestedAction;
    private String dataDescription;
    private String treatmentDatePeriod;
    private String supportingDocuments;

}

