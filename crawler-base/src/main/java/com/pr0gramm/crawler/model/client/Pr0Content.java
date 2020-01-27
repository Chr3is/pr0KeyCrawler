package com.pr0gramm.crawler.model.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Pr0Content {

    private List<Pr0Post> posts = new ArrayList<>();

    public List<Pr0Post> getPosts() {
        return posts.stream().sorted().collect(Collectors.toList());
    }

    public List<Pr0Post> getPostsAfter(long date) {
        return getPosts().stream().filter(post -> post.getCreated() > date).collect(Collectors.toList());
    }

    public Pr0Post getLatestPost() {
        return getPosts().stream().findFirst().orElse(null);
    }

}
