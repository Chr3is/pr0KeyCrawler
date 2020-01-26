package com.pr0gramm.crawler.api.listeners.images;

import com.pr0gramm.crawler.model.client.Pr0Post;
import reactor.util.function.Tuple2;

import java.util.List;

public interface ImageTextListener {

    default void processImageText(List<Tuple2<Pr0Post, String>> imageTextsByPosts) {
    }
}
