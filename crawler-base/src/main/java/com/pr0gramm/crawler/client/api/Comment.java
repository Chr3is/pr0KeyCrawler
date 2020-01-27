package com.pr0gramm.crawler.client.api;

import lombok.Data;

@Data
public class Comment {

    private long id;

    private long parent;

    private String content;

    private long created;

    private int up;

    private int down;

    private String name;
}
