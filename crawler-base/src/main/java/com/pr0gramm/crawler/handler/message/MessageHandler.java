package com.pr0gramm.crawler.handler.message;

import com.pr0gramm.crawler.api.listeners.messages.Pr0grammMessagesListener;
import com.pr0gramm.crawler.handler.Handler;
import com.pr0gramm.crawler.model.Pr0User;
import com.pr0gramm.crawler.model.client.Pr0Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MessageHandler implements Handler<List<Tuple2<Pr0User, List<Pr0Message>>>> {

    private final List<Pr0grammMessagesListener> pr0grammMessagesListeners;

    @Override
    public Mono<Void> process(List<Tuple2<Pr0User, List<Pr0Message>>> input) {
        return Mono.just(input)
                .doOnNext(messagesByUser -> pr0grammMessagesListeners.forEach(listener -> listener.processMessages(messagesByUser)))
                .then();
    }
}
