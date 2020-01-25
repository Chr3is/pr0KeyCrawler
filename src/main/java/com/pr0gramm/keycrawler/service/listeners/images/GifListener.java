package com.pr0gramm.keycrawler.service.listeners.images;

import com.pr0gramm.keycrawler.api.Post;
import org.springframework.core.io.ByteArrayResource;
import reactor.util.function.Tuple2;

import java.util.List;

public interface GifListener {

    default void processGif(List<Tuple2<Post, ByteArrayResource>> gifsByPosts) {
    }
}
