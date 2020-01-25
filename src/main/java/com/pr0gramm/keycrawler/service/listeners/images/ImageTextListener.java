package com.pr0gramm.keycrawler.service.listeners.images;

import com.pr0gramm.keycrawler.api.Post;
import reactor.util.function.Tuple2;

import java.util.List;

public interface ImageTextListener {

    default void processImageText(List<Tuple2<Post, String>> imageTextsByPosts) {
    }
}
