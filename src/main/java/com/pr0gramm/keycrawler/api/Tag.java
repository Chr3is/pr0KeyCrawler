package com.pr0gramm.keycrawler.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Tag {

    private long id;

    @JsonProperty("tag")
    private String name;
}
