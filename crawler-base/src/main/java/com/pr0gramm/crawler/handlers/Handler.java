package com.pr0gramm.crawler.handlers;

import com.pr0gramm.crawler.api.model.PostType;
import reactor.core.publisher.Mono;

public interface Handler<T> {

    Mono<Void> process(T input);

    default boolean supports(PostType type) {
        return false;
    }
}
