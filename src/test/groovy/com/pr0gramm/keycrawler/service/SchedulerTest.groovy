package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.model.KeyResult
import com.pr0gramm.keycrawler.service.telegram.TelegramBot
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class SchedulerTest extends Specification {

    KeyCrawler keyCrawler = Mock()

    RegistrationService registrationService = Mock()

    Pr0grammCommentService commentService = Mock()

    TelegramBot telegramBot = Mock()

    @Subject
    Scheduler scheduler = new Scheduler(keyCrawler, Optional.of(registrationService), Optional.of(telegramBot), Optional.of(commentService))

    def 'checkForNewRegistrations invokes userservice'() {
        when:
        scheduler.checkForNewRegistrations()

        then:
        1 * registrationService.handleNewRegistrations() >> Mono.empty()
    }

    def 'checkForNewKes wont send message if there are no keys'() {
        given:
        keyCrawler.checkForNewKeys() >> Mono.just([])

        when:
        scheduler.checkForNewKeys()

        then:
        0 * telegramBot.sendMessage(_)
    }

    @Unroll
    def 'checkForNewKeys crawls keys and sends as message for result=#result'(List<KeyResult> result, int invokCount) {
        when:
        scheduler.checkForNewKeys()

        then:
        1 * keyCrawler.checkForNewKeys() >> Mono.just(result)
        invokCount * telegramBot.sendMessage(result) >> Mono.empty()
        invokCount * commentService.sendNewComment(result) >> Mono.empty()

        where:
        result                                                || invokCount
        []                                                    || 0
        [new KeyResult(Tuples.of(new Post(), 'Hello World'))] || 1
    }

}
