package com.pr0gramm.keycrawler.model

import com.pr0gramm.keycrawler.api.Content
import com.pr0gramm.keycrawler.api.Post
import spock.lang.Specification

class ContentTest extends Specification {

    def 'content returns posts sorted'() {
        given:
        Post oldPost = new Post(created: 5)
        Post someOtherPost = new Post(created: 2)
        Post latestPost = new Post(created: 10)

        Content content = new Content(posts: [someOtherPost, latestPost, oldPost, someOtherPost])

        expect:
        content.getPosts().get(0).getCreated() == 10
    }

    def 'content returns posts after given date sorted'() {
        given:
        Post oldPost = new Post(created: 5)
        Post someOtherPost = new Post(created: 2)
        Post latestPost = new Post(created: 10)

        Content content = new Content(posts: [someOtherPost, latestPost, oldPost, someOtherPost])

        when:
        List<Post> posts = content.getPostsAfter(2)

        then:
        posts.size() == 2
        content.getPosts().get(0).getCreated() == 10
    }

    def 'content returns latest post correctly'() {
        given:
        Post oldPost = new Post(created: 5)
        Post someOtherPost = new Post(created: 2)
        Post latestPost = new Post(created: 10)

        Content content = new Content(posts: [someOtherPost, latestPost, oldPost, someOtherPost])

        expect:
        content.getLatestPost() == latestPost
    }

    def 'full url is set correctly for all posts'() {
        given:
        Post post1 = new Post(id: 1)
        Post post2 = new Post(id: 2)

        Content content = new Content(posts: [post1, post2])

        when:
        content = content.setFullPostUrl("https://test.com/")

        then:
        content.posts[0].fullUrl == "https://test.com/1"
        content.posts[1].fullUrl == "https://test.com/2"
    }

}
