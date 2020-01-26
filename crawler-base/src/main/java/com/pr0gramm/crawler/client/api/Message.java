package com.pr0gramm.crawler.client.api;

import lombok.Data;

@Data
public class Message {

    private long id;

    private long itemId;

    private long senderId;

    private String name;

    private String message;

    private long created;

}
