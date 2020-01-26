package com.pr0gramm.crawler.api.listeners.videos;

import com.pr0gramm.crawler.model.client.Pr0Post;
import org.springframework.core.io.ByteArrayResource;
import reactor.util.function.Tuple2;

import java.util.List;

public interface VideoListener {

    default void processVideo(List<Tuple2<Pr0Post, ByteArrayResource>> imagesByPosts) {
    }

}
