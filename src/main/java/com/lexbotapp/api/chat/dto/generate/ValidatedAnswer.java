package com.lexbotapp.api.chat.dto.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class ValidatedAnswer {

    private static final ObjectMapper mapper = new ObjectMapper();

    private String error;
    private String result;

    public static ValidatedAnswer parse(String json) throws Exception {
        return mapper.readValue(json, ValidatedAnswer.class);
    }

}
