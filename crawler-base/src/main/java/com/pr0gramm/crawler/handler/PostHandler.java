package com.pr0gramm.crawler.handler;

import com.pr0gramm.crawler.api.listeners.PostListener;
import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostHandler implements Handler<List<Pr0Post>> {

    private final List<Handler<Mono<List<Pr0Post>>>> handlers;

    private final List<PostListener> postListeners;

    @Override
    public Mono<Void> process(List<Pr0Post> input) {
        return Mono.just(input)
                .doOnNext(posts -> postListeners.forEach(listener -> listener.processPost(posts)))
                .flatMapIterable(posts -> posts)
                .groupBy(Pr0Post::getType)
                .map(postsByType -> handlers.stream()
                        .filter(handler -> handler.supports(postsByType.key()))
                        .map(monoHandler -> monoHandler.process(postsByType.collectList())))
                .collectList()
                .then();
    }
}
