package com.pr0gramm.keycrawler.service.handlers;

import com.pr0gramm.keycrawler.api.Post;
import com.pr0gramm.keycrawler.model.PostType;
import com.pr0gramm.keycrawler.service.listeners.PostListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostHandler implements Handler<List<Post>> {

    private final List<Handler<Mono<List<Post>>>> handlers;

    private final List<PostListener> postListeners;

    @Override
    public Mono<Void> process(List<Post> input) {
        return Mono.just(input)
                .doOnNext(posts -> postListeners.forEach(listener -> listener.processPost(posts)))
                .flatMapIterable(posts -> posts)
                .groupBy(PostType::getFrom)
                .flatMap(postsByType -> handlers.stream().filter(handler -> handler.isSupported(postsByType.key()))
                        .findFirst().map(handler -> handler.process(postsByType.collectList())).orElse(Mono.empty()))
                .collectList()
                .then();
    }
}
