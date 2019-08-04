package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.client.Pr0grammClient;
import com.pr0gramm.keycrawler.model.Pr0User;
import com.pr0gramm.keycrawler.model.Pr0grammMessage;
import com.pr0gramm.keycrawler.repository.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.pr0gramm.keycrawler.config.MessageCodes.PR0GRAMM_MSG_REGISTRATION;
import static com.pr0gramm.keycrawler.service.MessageBundleResolver.getMessage;
import static com.pr0gramm.keycrawler.service.UserService.USER_TOKEN_DELIMITER;

@RequiredArgsConstructor
@Service
public class Pr0grammMessageService {

    private final Pr0grammClient pr0grammClient;

    public Flux<Pr0User> getUsersWithPendingMessages() {
        return pr0grammClient.getUserWithPendingMessages()
                .doOnNext(pr0User -> pr0grammClient.getMessagesWith(pr0User).subscribe());
    }

    public Mono<Void> sendNewMessage(User user) {
        return pr0grammClient.sendNewMessage(createMessage(user));
    }

    private Pr0grammMessage createMessage(User user) {
        String message = String.format(getMessage(PR0GRAMM_MSG_REGISTRATION), user.getUserName() + USER_TOKEN_DELIMITER + user.getToken());
        return new Pr0grammMessage(user.getProUserId(), user.getUserName(), message);
    }

}
