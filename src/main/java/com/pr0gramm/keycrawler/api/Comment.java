package com.pr0gramm.keycrawler.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Comment {

    private long id;

    @JsonProperty("parent")
    private long parentId;

    private String content;

    private long created;

    private int up;

    private int down;

    @JsonProperty("name")
    private String userName;
}
