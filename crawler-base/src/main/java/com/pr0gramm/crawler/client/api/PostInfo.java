package com.pr0gramm.crawler.client.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostInfo {

    private List<Tag> tags = new ArrayList<>();

    private List<Comment> comments = new ArrayList<>();

}
