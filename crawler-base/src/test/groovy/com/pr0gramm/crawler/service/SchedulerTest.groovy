package com.pr0gramm.crawler.service


import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class SchedulerTest extends Specification {

    Crawler crawler = Mock()

    Pr0grammMessageService messageService = Mock()

    @Subject
    Scheduler scheduler = new Scheduler(crawler, messageService)

    /*TODO not part anymore
    def 'checkForNewRegistrations invokes userservice'() {
        when:
        scheduler.checkForNewRegistrations()

        then:
        1 * registrationService.handleNewRegistrations() >> Mono.empty()
    }*/

    /* TODO not part anymore
    def 'checkForNewKes wont send message if there are no keys'() {
        given:
        crawler.checkForNewPosts() >> Mono.just([])

        when:
        scheduler.checkForNewKeys()

        then:
        0 * telegramBot.sendMessage(_)
    }*/

    @Unroll
    def 'crawler is invoked'() {
        when:
        scheduler.checkForNewPosts()

        then:
        1 * crawler.checkForNewPosts() >> Mono.empty()
        /* TODO not part anymore
        invokCount * telegramBot.sendMessage(result) >> Mono.empty()
        invokCount * commentService.sendNewComment(result) >> Mono.empty()

        where:
        result                                                || invokCount
        []                                                    || 0
        [new KeyResult(Tuples.of(new Post(), 'Hello World'))] || 1
      */
    }


}
