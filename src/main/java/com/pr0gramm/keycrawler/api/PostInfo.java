package com.pr0gramm.keycrawler.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostInfo {

    private List<Tag> tags = new ArrayList<>();

    private List<Comment> comments = new ArrayList<>();

}
