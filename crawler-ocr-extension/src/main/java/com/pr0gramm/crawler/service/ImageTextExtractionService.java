package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.api.listeners.images.ImageListener;
import com.pr0gramm.crawler.handler.image.ImageTextHandler;
import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
public class ImageTextExtractionService implements ImageListener {

    private final ImageTextHandler imageTextHandler;

    public void processImage(List<Tuple2<Pr0Post, ByteArrayResource>> imagesByPosts) {
        imageTextHandler.process(imagesByPosts);
    }

}
