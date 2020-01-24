package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.client.Pr0grammClient;
import com.pr0gramm.keycrawler.config.properties.NotificationProperties;
import com.pr0gramm.keycrawler.model.KeyResult;
import com.pr0gramm.keycrawler.model.Pr0grammComment;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.pr0gramm.keycrawler.model.MessageCodes.PR0GRAMM_MSG_CRAWLED_POST;
import static com.pr0gramm.keycrawler.service.MessageBundleResolver.getMessage;

@RequiredArgsConstructor
public class Pr0grammCommentService {

    private final NotificationProperties notificationProperties;

    private final Pr0grammClient pr0grammClient;

    public Mono<Void> sendNewComment(List<KeyResult> results) {
        return Flux.fromIterable(results)
                .flatMap(keyResult -> pr0grammClient.postNewComment(createCommentFrom(keyResult)))
                .then();
    }

    private Pr0grammComment createCommentFrom(KeyResult result) {
        String message = String.format(getMessage(PR0GRAMM_MSG_CRAWLED_POST),
                result.getPost().getUser(), result.getKeysFormatted(), notificationProperties.getHowToPost());
        return new Pr0grammComment(result.getPost().getId(), message);
    }
}
