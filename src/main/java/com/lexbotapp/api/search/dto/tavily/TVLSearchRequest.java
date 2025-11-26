package com.lexbotapp.api.search.dto.tavily;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TVLSearchRequest {

    private String query;
    private String search_depth;
    private boolean include_raw_content;
    private String country;
    private int max_results;

}
