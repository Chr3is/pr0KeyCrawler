package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.model.KeyResult
import com.pr0gramm.keycrawler.service.telegram.TelegramBot
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class SchedulerTest extends Specification {

    KeyCrawler keyCrawler = Mock()

    UserService userService = Mock()

    TelegramBot telegramBot = Mock()

    @Subject
    Scheduler scheduler = new Scheduler(keyCrawler, userService, Optional.of(telegramBot))

    @Unroll
    def 'initialFetch sets authentication status to #status'(boolean status) {
        given:
        keyCrawler.init() >> Mono.just(status)

        when:
        scheduler.initialFetch()

        then:
        scheduler.authenticated == status

        where:
        status << [true, false]
    }

    def 'checkForNewRegistrations invokes userservice'() {
        when:
        scheduler.checkForNewRegistrations()

        then:
        1 * userService.handleNewRegistrations() >> Flux.empty()
    }

    def 'checkForNewKes wont send message if there are no keys'() {
        given:
        keyCrawler.checkForNewKeys(_) >> Mono.just([])

        when:
        scheduler.checkForNewKeys()

        then:
        0 * telegramBot.sendMessage(_)
    }

    @Unroll
    def 'checkForNewKeys crawls keys and sends as message'(List<KeyResult> result, int invokCount) {
        when:
        scheduler.checkForNewKeys()

        then:
        1 * keyCrawler.checkForNewKeys(_) >> Mono.just(result)
        invokCount * telegramBot.sendMessage(result) >> Mono.empty()

        where:
        result                                                || invokCount
        []                                                    || 0
        [new KeyResult(Tuples.of(new Post(), 'Hello World'))] || 1
    }

}
