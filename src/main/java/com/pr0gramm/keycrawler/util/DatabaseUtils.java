package com.pr0gramm.keycrawler.util;

import com.pr0gramm.keycrawler.service.exception.DatabaseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@UtilityClass
@Slf4j
public class DatabaseUtils {

    public static <T> Flux<T> handleDbRequest(Flux<T> flux) {
        return flux.onErrorResume(throwable -> {
            log.error("Error while executing database request", throwable);
            return Mono.error(new DatabaseException("Error while executing database request", throwable));
        });
    }

    public static <T> Mono<T> handleDbRequest(Mono<T> mono) {
        return mono.onErrorResume(throwable -> {
            log.error("Error while executing database request", throwable);
            return Mono.error(new DatabaseException("Error while executing database request", throwable));
        });
    }

}
