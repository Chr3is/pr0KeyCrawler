package com.pr0gramm.crawler.client;

import com.pr0gramm.crawler.api.model.Pr0Comment;
import com.pr0gramm.crawler.api.model.Pr0Message;
import com.pr0gramm.crawler.model.client.Pr0Content;
import com.pr0gramm.crawler.model.client.Pr0Messages;
import reactor.core.publisher.Mono;

public interface ApiClient {

    Mono<Pr0Content> fetchNewContent();

    Mono<Pr0Messages> getPendingMessagesByUser();

    Mono<Pr0Messages> getMessagesWith(String userName);

    Mono<Void> sendNewMessage(Pr0Message message);

    Mono<Void> postNewComment(Pr0Comment comment);
}
