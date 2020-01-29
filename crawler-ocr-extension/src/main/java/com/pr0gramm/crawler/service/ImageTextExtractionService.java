package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.api.listeners.images.ImageListener;
import com.pr0gramm.crawler.handler.image.ImageTextHandler;
import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ImageTextExtractionService implements ImageListener {

    private final ImageTextHandler imageTextHandler;

    public void processImage(List<Tuple2<Pr0Post, ByteArrayResource>> imagesByPosts) {
        log.debug("Processing posts (count={})", imagesByPosts.size());
        imageTextHandler.process(imagesByPosts).subscribe();
    }

}
