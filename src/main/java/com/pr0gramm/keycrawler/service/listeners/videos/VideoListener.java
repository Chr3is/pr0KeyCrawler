package com.pr0gramm.keycrawler.service.listeners.videos;

import com.pr0gramm.keycrawler.api.Post;
import org.springframework.core.io.ByteArrayResource;
import reactor.util.function.Tuple2;

import java.util.List;

public interface VideoListener {

    default void processVideo(List<Tuple2<Post, ByteArrayResource>> imagesByPosts) {
    }

}
