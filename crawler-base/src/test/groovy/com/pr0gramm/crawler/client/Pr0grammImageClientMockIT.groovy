package com.pr0gramm.crawler.client


import com.pr0gramm.crawler.config.Pr0grammImageClientConfig
import com.pr0gramm.crawler.model.client.Pr0Post
import org.apache.commons.lang3.RandomUtils
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.test.context.TestPropertySource
import reactor.util.function.Tuple2
import spock.lang.Shared
import spock.lang.Specification

import static org.mockserver.integration.ClientAndServer.startClientAndServer

@SpringBootTest(classes = [WebClientAutoConfiguration, Pr0grammImageClientConfig])
@TestPropertySource(properties = ['pr0gramm.image-client.url=http://localhost:27119/img.pr0gramm.com/'])
class Pr0grammImageClientMockIT extends Specification {

    @Shared
    MockServerClient mockServerClient

    @Autowired
    Pr0grammImageClient imageClient

    def setupSpec() {
        mockServerClient = startClientAndServer(27119)
    }

    def cleanup() {
        mockServerClient.reset()
    }

    def cleanupSpec() {
        mockServerClient.stop()
    }

    def 'images can be fetched correctly'() {
        given:
        Pr0Post pr0Post = new Pr0Post(id: 1, contentLink: 'image/image1.jpg', user: 'TestUser')
        imageDownload(pr0Post)

        when:
        Tuple2<Pr0Post, ByteArrayResource> imageWithPost = imageClient.getContent(pr0Post).block()

        then:
        verifyAll(imageWithPost.t1) {
            id == 1
            contentLink == 'image/image1.jpg'
            user == 'TestUser'
        }
        imageWithPost.t2
    }

    def imageDownload(Pr0Post post) {
        mockServerClient.when(HttpRequest.request()
                .withPath("/img.pr0gramm.com/${post.contentLink}")
                .withHeader('Accept', 'application/octet-stream')
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/octet-stream')
                .withBody(RandomUtils.nextBytes(20)))
    }
}
