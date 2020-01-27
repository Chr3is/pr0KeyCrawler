package com.pr0gramm.crawler.model


import com.pr0gramm.crawler.model.client.Pr0Post
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class Pr0PostTest extends Specification {

    @Unroll
    def 'Post with created=#created returns isAfter=#isAfter when lastTimeAnalyzed=#lastTimeAnalyzed'(long lastTimeAnalyzed, long created, boolean isAfter) {
        given:
        Pr0Post post = new Pr0Post(created: created)

        expect:
        post.isAfter(lastTimeAnalyzed) == isAfter

        where:
        lastTimeAnalyzed | created || isAfter
        0                | 0       || false
        0                | 10      || true
        10               | 0       || false

    }

    def 'posts are sorted by created'() {
        given:
        Pr0Post oldPost = new Pr0Post(created: 5)
        Pr0Post latestPost = new Pr0Post(created: 10)

        List<Pr0Post> results = [oldPost, latestPost]

        expect:
        results.stream().sorted().collect(Collectors.toList()) == [latestPost, oldPost]
    }

    @Unroll
    def 'def fullUrl is set correctly'() {
        given:
        String url = 'https://localhost:8080/'
        long postId = 5
        Pr0Post post = new Pr0Post(id: postId)

        when:
        post.setFullUrl(url)

        then:
        post.fullUrl == url + postId
    }

}
