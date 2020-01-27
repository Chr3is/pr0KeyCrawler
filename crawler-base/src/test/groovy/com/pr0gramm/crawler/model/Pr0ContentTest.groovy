package com.pr0gramm.crawler.model


import com.pr0gramm.crawler.model.client.Pr0Content
import com.pr0gramm.crawler.model.client.Pr0Post
import spock.lang.Specification

class Pr0ContentTest extends Specification {

    def 'content returns posts sorted'() {
        given:
        Pr0Post oldPost = new Pr0Post(created: 5)
        Pr0Post someOtherPost = new Pr0Post(created: 2)
        Pr0Post latestPost = new Pr0Post(created: 10)

        Pr0Content content = new Pr0Content(posts: [someOtherPost, latestPost, oldPost, someOtherPost])

        expect:
        content.posts.get(0).getCreated() == 10
    }

    def 'content returns posts after given date sorted'() {
        given:
        Pr0Post oldPost = new Pr0Post(created: 5)
        Pr0Post someOtherPost = new Pr0Post(created: 2)
        Pr0Post latestPost = new Pr0Post(created: 10)

        Pr0Content content = new Pr0Content(posts: [someOtherPost, latestPost, oldPost, someOtherPost])

        when:
        List<Pr0Post> posts = content.getPostsAfter(2)

        then:
        posts.size() == 2
        content.posts.get(0).getCreated() == 10
    }

    def 'content returns latest post correctly'() {
        given:
        Pr0Post oldPost = new Pr0Post(created: 5)
        Pr0Post someOtherPost = new Pr0Post(created: 2)
        Pr0Post latestPost = new Pr0Post(created: 10)

        Pr0Content content = new Pr0Content(posts: [someOtherPost, latestPost, oldPost, someOtherPost])

        expect:
        content.getLatestPost() == latestPost
    }

}
