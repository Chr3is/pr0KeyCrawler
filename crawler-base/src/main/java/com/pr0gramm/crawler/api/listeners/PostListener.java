package com.pr0gramm.crawler.api.listeners;

import com.pr0gramm.crawler.model.client.Pr0Post;

import java.util.List;


public interface PostListener {

    default void processPost(List<Pr0Post> posts) {
    }

}
