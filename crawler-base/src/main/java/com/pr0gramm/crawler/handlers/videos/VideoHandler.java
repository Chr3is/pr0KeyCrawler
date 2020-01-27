package com.pr0gramm.crawler.handlers.videos;

import com.pr0gramm.crawler.api.listeners.videos.VideoListener;
import com.pr0gramm.crawler.client.Pr0grammVideoClient;
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
public class VideoHandler implements Handler<Mono<List<Pr0Post>>> {

    private static final Long DELAY_IN_MILLIS = 500L;

    private final List<VideoListener> videoListeners;

    private final Pr0grammVideoClient videoClient;

    @Override
    public Mono<Void> process(Mono<List<Pr0Post>> input) {
        return input.flatMapIterable(posts -> posts)
                .delayElements(Duration.of(DELAY_IN_MILLIS, ChronoUnit.MILLIS))
                .flatMap(videoClient::getContent)
                .collectList()
                .doOnNext(videos -> videoListeners.forEach(listener -> listener.processVideo(videos)))
                .then();
    }

    @Override
    public boolean supports(PostType type) {
        return type == PostType.VIDEO;
    }
}
