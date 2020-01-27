package com.pr0gramm.crawler.api.listeners.messages;

import com.pr0gramm.crawler.model.Pr0User;
import com.pr0gramm.crawler.model.client.Pr0Message;
import reactor.util.function.Tuple2;

import java.util.List;

public interface Pr0grammMessagesListener {

    default void processMessages(List<Tuple2<Pr0User, List<Pr0Message>>> messagesByUser) {

    }

}
