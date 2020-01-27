package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.api.model.NewPr0Comment;
import com.pr0gramm.crawler.client.ApiClient;
import com.pr0gramm.crawler.config.properties.NotificationProperties;
import com.pr0gramm.crawler.model.KeyResult;
import com.pr0gramm.crawler.model.MessageCodes;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class Pr0grammCommentService {

    private final NotificationProperties notificationProperties;

    private final ApiClient pr0grammClient;

    public Mono<Void> sendNewComment(List<KeyResult> results) {
        return Flux.fromIterable(results)
                .flatMap(keyResult -> pr0grammClient.postNewComment(createCommentFrom(keyResult)))
                .then();
    }

    private NewPr0Comment createCommentFrom(KeyResult result) {
        String message = String.format(MessageBundleResolver.getMessage(MessageCodes.PR0GRAMM_MSG_CRAWLED_POST),
                result.getPost().getUser(), result.getKeysFormatted(), notificationProperties.getHowToPost());
        return new NewPr0Comment(result.getPost(), message);
    }
}
