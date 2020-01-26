package com.pr0gramm.crawler.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Comment {

    private long id;

    private long parent;

    private String content;

    private long created;

    private int up;

    private int down;

    @JsonProperty("name")
    private String userName;
}
