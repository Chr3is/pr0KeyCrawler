package com.pr0gramm.crawler.model

import com.pr0gramm.crawler.client.api.Content
import com.pr0gramm.crawler.client.api.Post
import spock.lang.Specification

class ContentTest extends Specification {

    def 'content returns posts sorted'() {
        given:
        Post oldPost = new Post(created: 5)
        Post someOtherPost = new Post(created: 2)
        Post latestPost = new Post(created: 10)

        Content content = new Content(setItems: [someOtherPost, latestPost, oldPost, someOtherPost])

        expect:
        content.getItems().get(0).getCreated() == 10
    }

    def 'content returns posts after given date sorted'() {
        given:
        Post oldPost = new Post(created: 5)
        Post someOtherPost = new Post(created: 2)
        Post latestPost = new Post(created: 10)

        Content content = new Content(setItems: [someOtherPost, latestPost, oldPost, someOtherPost])

        when:
        List<Post> posts = content.getPostsAfter(2)

        then:
        posts.size() == 2
        content.getItems().get(0).getCreated() == 10
    }

    def 'content returns latest post correctly'() {
        given:
        Post oldPost = new Post(created: 5)
        Post someOtherPost = new Post(created: 2)
        Post latestPost = new Post(created: 10)

        Content content = new Content(setItems: [someOtherPost, latestPost, oldPost, someOtherPost])

        expect:
        content.getLatestPost() == latestPost
    }

    def 'full url is set correctly for all posts'() {
        given:
        Post post1 = new Post(id: 1)
        Post post2 = new Post(id: 2)

        Content content = new Content(setItems: [post1, post2])

        when:
        content = content.setFullPostUrl("https://test.com/")

        then:
        content.items[0].fullUrl == "https://test.com/1"
        content.items[1].fullUrl == "https://test.com/2"
    }

}
