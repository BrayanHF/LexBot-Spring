package com.lexbot.search.dto.tavily;

import lombok.Data;

@Data
public class Result {

    private String title;
    private String url;
    private String content;
    private String raw_content;

}
