package com.pr0gramm.keycrawler.util;

import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

import java.util.Optional;

@UtilityClass
public class OptionalUtils {

    public static <T> Mono<T> execute(Optional<Mono<T>> mono) {
        return mono.orElse(Mono.empty());
    }
}
