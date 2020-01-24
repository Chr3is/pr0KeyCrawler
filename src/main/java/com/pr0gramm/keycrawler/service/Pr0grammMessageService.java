package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.api.Message;
import com.pr0gramm.keycrawler.client.Pr0grammClient;
import com.pr0gramm.keycrawler.model.Pr0User;
import com.pr0gramm.keycrawler.model.Pr0grammMessage;
import com.pr0gramm.keycrawler.repository.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pr0gramm.keycrawler.model.MessageCodes.PR0GRAMM_MSG_REGISTRATION;
import static com.pr0gramm.keycrawler.service.MessageBundleResolver.getMessage;
import static com.pr0gramm.keycrawler.service.UserService.USER_TOKEN_DELIMITER;

@RequiredArgsConstructor
@Service
public class Pr0grammMessageService {

    private final Pr0grammClient pr0grammClient;

    public Flux<Tuple2<Pr0User, List<Message>>> getPendingMessages() {
        return pr0grammClient.getPendingMessagesByUser()
                .map(messages -> messages.getMessages().stream()
                        .filter(Message::isMessage)
                        .collect(Collectors.groupingBy(Message::getSenderId)))
                .flatMapIterable(Map::values)
                .filter(messages -> !messages.isEmpty())
                .map(messages -> {
                    Message message = messages.get(0);
                    return Tuples.of(new Pr0User(message.getSenderId(), message.getUserName()), messages);
                });
    }

    public Mono<Void> markMessagesAsReadFor(User user) {
        return pr0grammClient.getMessagesWith(user.getUserName()).then();
    }

    public Mono<Void> sendNewMessage(User user) {
        return pr0grammClient.sendNewMessage(createMessage(user));
    }

    private Pr0grammMessage createMessage(User user) {
        String message = String.format(getMessage(PR0GRAMM_MSG_REGISTRATION), user.getUserName() + USER_TOKEN_DELIMITER + user.getToken());
        return new Pr0grammMessage(user.getProUserId(), user.getUserName(), message);
    }
}
