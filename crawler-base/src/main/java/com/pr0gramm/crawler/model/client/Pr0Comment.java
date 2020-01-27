package com.pr0gramm.crawler.model.client;

import lombok.Data;

@Data
public class Pr0Comment {

    private long id;

    private long parent;

    private String content;

    private long created;

    private int up;

    private int down;

    private String userName;
}
