package com.pr0gramm.keycrawler.client;

import com.pr0gramm.keycrawler.api.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Slf4j
public abstract class Pr0grammContentClient {

    private final WebClient client;

    public Mono<Tuple2<Post, ByteArrayResource>> getContent(Post post) {
        return client.get()
                .uri(post.getContentLink())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .onStatus(httpStatus -> !httpStatus.is2xxSuccessful() && httpStatus.value() != 404, clientResponse -> {
                    log.error("Could not download content with uri={}", post.getContentLink());
                    return Mono.empty();
                })
                .bodyToMono(ByteArrayResource.class)
                .map(byteArrayResource -> Tuples.of(post, byteArrayResource));
    }

}
