package com.pr0gramm.keycrawler.service.listeners.messages;

import com.pr0gramm.keycrawler.api.Message;
import com.pr0gramm.keycrawler.model.Pr0User;
import reactor.util.function.Tuple2;

import java.util.List;

public interface Pr0grammMessagesListener {

    default void processMessages(List<Tuple2<Pr0User, List<Message>>> messagesByUser) {

    }

}
