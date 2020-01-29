package com.pr0gramm.crawler.handler

import com.pr0gramm.crawler.api.listeners.PostListener
import com.pr0gramm.crawler.model.PostType
import com.pr0gramm.crawler.model.client.Pr0Post
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

class PostHandlerTest extends Specification {

    PostListener postListener = Mock()

    Handler<Mono<List<Pr0Post>>> handler = Mock()

    @Subject
    PostHandler postHandler = new PostHandler([handler], [postListener])

    def 'test'() {
        given:
        PostType type = PostType.IMAGE
        List<Pr0Post> posts = [new Pr0Post(type: type)]

        when:
        postHandler.process(posts).block()

        then:
        1 * postListener.processPost(posts)
        1 * handler.supports(type) >> true
        1 * handler.process(_) >> Mono.empty()
    }


}
