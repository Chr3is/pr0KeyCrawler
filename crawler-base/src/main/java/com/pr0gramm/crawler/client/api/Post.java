package com.pr0gramm.crawler.client.api;

import lombok.Data;

@Data
public class Post {

    private long id;

    private long created = 0;

    private String image;

    private String user;

}
