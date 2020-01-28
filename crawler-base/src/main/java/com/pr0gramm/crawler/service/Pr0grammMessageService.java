package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.api.model.NewPr0Message;
import com.pr0gramm.crawler.client.Pr0grammClient;
import com.pr0gramm.crawler.handler.message.MessageHandler;
import com.pr0gramm.crawler.model.Pr0User;
import com.pr0gramm.crawler.model.client.Pr0Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class Pr0grammMessageService {

    private final Pr0grammClient pr0grammClient;

    private final MessageHandler messageHandler;

    public Mono<Void> checkForNewPendingMessages() {
        return pr0grammClient.getPendingMessagesByUser()
                .map(messages -> messages.getMessages().stream()
                        .filter(Pr0Message::isTypeMessage)
                        .collect(Collectors.groupingBy(Pr0Message::getSenderId)))
                .flatMapIterable(Map::values)
                .filter(messages -> !messages.isEmpty())
                .map(messages -> {
                    Pr0Message message = messages.get(0);
                    return Tuples.of(new Pr0User(message.getSenderId(), message.getUserName()), messages);
                })
                .collectList()
                .doOnNext(messageHandler::process)
                .then();
    }

    public Mono<Void> markMessagesAsReadFor(Pr0User user) {
        return pr0grammClient.getMessagesWith(user).then();
    }

    public Mono<Void> sendNewMessage(NewPr0Message message) {
        return pr0grammClient.sendNewMessage(message);
    }

}
