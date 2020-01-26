package com.pr0gramm.crawler.client;

import com.pr0gramm.crawler.model.client.Pr0Post;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface ContentClient {

    Mono<Tuple2<Pr0Post, ByteArrayResource>> getContent(Pr0Post post);
}
