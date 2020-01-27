package com.pr0gramm.crawler.client;

import com.pr0gramm.crawler.api.model.NewPr0Comment;
import com.pr0gramm.crawler.api.model.NewPr0Message;
import com.pr0gramm.crawler.model.Pr0User;
import com.pr0gramm.crawler.model.client.Pr0Content;
import com.pr0gramm.crawler.model.client.Pr0Messages;
import reactor.core.publisher.Mono;

public interface ApiClient {

    Mono<Pr0Content> fetchNewContent();

    Mono<Pr0Messages> getPendingMessagesByUser();

    Mono<Pr0Messages> getMessagesWith(Pr0User userName);

    Mono<Void> sendNewMessage(NewPr0Message message);

    Mono<Void> postNewComment(NewPr0Comment comment);
}
