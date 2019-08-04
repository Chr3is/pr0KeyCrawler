package com.pr0gramm.keycrawler.model


import com.pr0gramm.keycrawler.api.Post
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class PostTest extends Specification {

    @Unroll
    def 'post with image=#image is supported=#isSupported'(String image, boolean isSupported) {
        given:
        Post post = new Post(image: image)

        expect:
        post.supported == isSupported

        where:
        image        || isSupported
        'video.mp4'  || false
        'gif.gif'    || false
        'webm.webm'  || false
        'image.jpg'  || true
        'image.jpeg' || true
        'image.jpe'  || true
        'image.png'  || true
    }

    @Unroll
    def 'Post with created=#created returns isAfter=#isAfter when lastTimeAnalyzed=#lastTimeAnalyzed'(long lastTimeAnalyzed, long created, boolean isAfter) {
        given:
        Post post = new Post(created: created)

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
        Post oldPost = new Post(created: 5)
        Post latestPost = new Post(created: 10)

        List<Post> results = [oldPost, latestPost]

        expect:
        results.stream().sorted().collect(Collectors.toList()) == [latestPost, oldPost]
    }

    @Unroll
    def 'type=#result is returned for image=#image correctly'(String image, String result) {
        given:
        Post post = new Post(image: image)

        expect:
        post.getImageType() == result

        where:
        image      || result
        'test.png' || 'png'
        'test.jpg' || 'jpg'
        'abc'      || ''
    }

}
