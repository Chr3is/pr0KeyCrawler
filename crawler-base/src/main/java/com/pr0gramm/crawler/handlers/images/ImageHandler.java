package com.pr0gramm.crawler.handlers.images;

import com.pr0gramm.crawler.api.listeners.images.ImageListener;
import com.pr0gramm.crawler.client.Pr0grammImageClient;
import com.pr0gramm.crawler.handlers.Handler;
import com.pr0gramm.crawler.model.PostType;
import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageHandler implements Handler<Mono<List<Pr0Post>>> {

    private static final Long DELAY_IN_MILLIS = 500L;

    private final ImageTextHandler imageTextHandler;

    private final List<ImageListener> imageListeners;

    private final Pr0grammImageClient imageClient;

    @Override
    public Mono<Void> process(Mono<List<Pr0Post>> input) {
        return input.flatMapIterable(posts -> posts)
                .delayElements(Duration.of(DELAY_IN_MILLIS, ChronoUnit.MILLIS))
                .flatMap(imageClient::getContent)
                .collectList()
                .doOnNext(images -> imageListeners.forEach(imageListener -> imageListener.processImage(images)))
                .flatMap(imageTextHandler::process)
                .then();
    }

    @Override
    public boolean supports(PostType type) {
        return type == PostType.IMAGE;
    }
}
