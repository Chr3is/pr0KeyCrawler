package com.pr0gramm.crawler.handler.image;

import com.pr0gramm.crawler.api.listeners.images.GifListener;
import com.pr0gramm.crawler.client.Pr0grammImageClient;
import com.pr0gramm.crawler.handler.Handler;
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
public class GifHandler implements Handler<Mono<List<Pr0Post>>> {

    private static final Long DELAY_IN_MILLIS = 500L;

    private final List<GifListener> gifListeners;

    private final Pr0grammImageClient imageClient;

    @Override
    public Mono<Void> process(Mono<List<Pr0Post>> input) {
        return input.flatMapIterable(posts -> posts)
                .delayElements(Duration.of(DELAY_IN_MILLIS, ChronoUnit.MILLIS))
                .flatMap(imageClient::getContent)
                .collectList()
                .doOnNext(gifs -> gifListeners.forEach(listener -> listener.processGif(gifs)))
                .then();
    }

    @Override
    public boolean supports(PostType type) {
        return type == PostType.GIF;
    }
}
