package com.pr0gramm.keycrawler.service.handlers.videos;

import com.pr0gramm.keycrawler.api.Post;
import com.pr0gramm.keycrawler.client.Pr0grammVideoClient;
import com.pr0gramm.keycrawler.model.PostType;
import com.pr0gramm.keycrawler.service.handlers.Handler;
import com.pr0gramm.keycrawler.service.listeners.videos.VideoListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoHandler implements Handler<Mono<List<Post>>> {

    private static final Long DELAY_IN_MILLIS = 500L;

    private final List<VideoListener> videoListeners;

    private final Pr0grammVideoClient videoClient;

    @Override
    public Mono<Void> process(Mono<List<Post>> input) {
        return input.flatMapIterable(posts -> posts)
                .delayElements(Duration.of(DELAY_IN_MILLIS, ChronoUnit.MILLIS))
                .flatMap(videoClient::getContent)
                .collectList()
                .doOnNext(videos -> videoListeners.forEach(imageListener -> imageListener.processVideo(videos)))
                .then();
    }

    @Override
    public boolean isSupported(PostType type) {
        return type == PostType.VIDEO;
    }
}
