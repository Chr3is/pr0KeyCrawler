package com.pr0gramm.keycrawler.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Content {

    @JsonProperty("items")
    private List<Post> posts = new ArrayList<>();

    public List<Post> getPosts() {
        return posts.stream().sorted().collect(Collectors.toList());
    }

    public List<Post> getPostsAfter(long date) {
        return getPosts().stream().filter(post -> post.getCreated() > date).collect(Collectors.toList());
    }

    public Post getLatestPost() {
        return getPosts().stream().findFirst().orElse(null);
    }

    public Content setFullPostUrl(String url) {
        posts.forEach(post -> post.setFullUrl(url));
        return this;
    }

}
