package com.pr0gramm.keycrawler.service.handlers;

import com.pr0gramm.keycrawler.model.PostType;
import reactor.core.publisher.Mono;

public interface Handler<T> {

    Mono<Void> process(T input);

    default boolean isSupported(PostType type) {
        return false;
    }
}
