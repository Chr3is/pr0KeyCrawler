package com.pr0gramm.crawler.handler;

import com.pr0gramm.crawler.model.PostType;
import reactor.core.publisher.Mono;

public interface Handler<T> {

    Mono<Void> process(T input);

    default boolean supports(PostType type) {
        return false;
    }
}
