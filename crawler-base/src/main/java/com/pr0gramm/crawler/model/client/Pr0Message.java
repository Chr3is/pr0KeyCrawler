package com.pr0gramm.crawler.model.client;

import lombok.Data;

@Data
public class Pr0Message {

    private long id;

    private long itemId;

    private long senderId;

    private String userName;

    private String message;

    private long created;


    public boolean isTypeMessage() {
        return itemId == 0;
    }

}
