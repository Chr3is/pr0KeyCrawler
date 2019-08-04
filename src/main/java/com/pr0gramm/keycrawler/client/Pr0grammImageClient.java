package com.pr0gramm.keycrawler.client;

import com.pr0gramm.keycrawler.api.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Service
@Slf4j
public class Pr0grammImageClient {

    private final WebClient pr0ImageClient;

    public Mono<Tuple2<Post, ByteArrayResource>> getImage(Post post) {
        return pr0ImageClient.get()
                .uri(post.getImage())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .onStatus(httpStatus -> !httpStatus.is2xxSuccessful() && httpStatus.value() != 404, clientResponse -> {
                    log.error("Could not download image with uri={}", post.getImage());
                    return Mono.empty();
                })
                .bodyToMono(ByteArrayResource.class)
                .map(byteArrayResource -> Tuples.of(post, byteArrayResource));
    }

}
