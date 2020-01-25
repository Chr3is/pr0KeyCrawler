package com.pr0gramm.keycrawler.service.listeners;

import com.pr0gramm.keycrawler.api.Post;

import java.util.List;


public interface PostListener {

    default void processPost(List<Post> posts) {
    }

}
