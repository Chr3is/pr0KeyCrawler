package com.pr0gramm.crawler.api.listeners.images;

import com.pr0gramm.crawler.model.client.Pr0Post;
import org.springframework.core.io.ByteArrayResource;
import reactor.util.function.Tuple2;

import java.util.List;

public interface GifListener {

    default void processGif(List<Tuple2<Pr0Post, ByteArrayResource>> gifsByPosts) {
    }
}
