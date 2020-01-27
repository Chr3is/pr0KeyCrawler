package com.pr0gramm.crawler.service

import com.pr0gramm.crawler.client.Pr0grammClient
import com.pr0gramm.crawler.handlers.PostHandler
import com.pr0gramm.crawler.model.client.Pr0Content
import com.pr0gramm.crawler.model.client.Pr0Post
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

class CrawlerTest extends Specification {

    Pr0Post post = new Pr0Post(id: 1, contentLink: 'abc.png', user: 'awesomeDude', created: 100)

    Pr0grammClient pr0grammApiClient = Mock()

    PostHandler postHandler = Mock()

    @Subject
    Crawler keyCrawler = new Crawler(pr0grammApiClient, postHandler)

    def 'init tries to login authenticated in and sets time of the latest post'() {
        when:
        keyCrawler.init().block()

        then:
        keyCrawler.dateTimeOfLastAnalyzedPost.get() == post.created

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.just(new Pr0Content(posts: [post]))
    }

    def 'post can be crawled'() {
        when:
        keyCrawler.checkForNewPosts().block()

        then:
        keyCrawler.dateTimeOfLastAnalyzedPost.get() == post.created

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.just(new Pr0Content(posts: [post]))
        1 * postHandler.process([post]) >> Mono.empty()
    }

    def 'posts which were already analyzed are skipped'() {
        given:
        keyCrawler.dateTimeOfLastAnalyzedPost.set(1000)

        when:
        keyCrawler.checkForNewPosts().block()

        then:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.empty()
        0 * postHandler._
    }

    def 'time will not be updated if there were no posts'() {
        when:
        keyCrawler.checkForNewPosts()

        then:
        keyCrawler.dateTimeOfLastAnalyzedPost.get() == 0

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.empty()
    }
}
