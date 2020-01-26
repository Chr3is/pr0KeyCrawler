package com.pr0gramm.crawler.client.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Content {

    private List<Post> items = new ArrayList<>();

}
