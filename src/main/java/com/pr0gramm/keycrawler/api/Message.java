package com.pr0gramm.keycrawler.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Message {

    private long id;

    private long itemId;

    private long senderId;

    @JsonProperty("name")
    private String userName;

    private String message;

    private long created;

    public boolean isMessage() {
        return itemId == 0;
    }

}
