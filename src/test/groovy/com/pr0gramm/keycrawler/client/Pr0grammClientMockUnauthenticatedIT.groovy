package com.pr0gramm.keycrawler.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.pr0gramm.keycrawler.api.Content
import com.pr0gramm.keycrawler.api.Message
import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.api.PostInfo
import com.pr0gramm.keycrawler.config.Pr0grammApiClientConfig
import com.pr0gramm.keycrawler.model.Nonce
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.model.Pr0grammComment
import com.pr0gramm.keycrawler.model.Pr0grammMessage
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification

import static org.mockserver.integration.ClientAndServer.startClientAndServer

@SpringBootTest(classes = [WebClientAutoConfiguration, JacksonAutoConfiguration, Pr0grammApiClientConfig, Nonce])
@TestPropertySource(properties = [
        'pr0gramm.api-client.url=http://localhost:27119/pr0gramm.com/',
        'pr0gramm.api-client.cookies[me]=%7B%22n%22%3A%22Test%22%2C%22id%22%3A%22123456789abcdefghijk%22%7D'])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class Pr0grammClientMockUnauthenticatedIT extends Specification {

    static final String ME_COOKIE = '%7B%22n%22%3A%22Test%22%2C%22id%22%3A%22123456789abcdefghijk%22%7D'

    @Shared
    MockServerClient mockServerClient

    @Autowired
    Pr0grammClient pr0grammClient

    @Autowired
    ObjectMapper objectMapper

    def setupSpec() {
        mockServerClient = startClientAndServer(27119)
        // The Pr0grammClient will execute a request while being configure to check if it can be use as authenticated
        // We have to response to this with request with this mocked response (403 leads to an unauthenticated client)
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/items/get')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
                .withQueryStringParameter('flags', '15')
        ).respond(HttpResponse.response()
                .withStatusCode(403)
                .withHeader('Content-Type', 'application/json')
        )
    }

    def setup() {
        mockServerClient.reset()
    }

    def cleanup() {
        mockServerClient.reset()
    }

    def cleanupSpec() {
        mockServerClient.stop()
    }

    def 'client is unauthenticated'() {
        expect:
        !pr0grammClient.metaPropertyValues[0].bean.isAuthenticated
    }

    def 'new content is fetched correctly'() {
        given:
        Post post = new Post(id: 1, image: 'image/image1.jpg', user: 'TestUser')
        createNewContent([post])

        when:
        Content newContent = pr0grammClient.fetchNewContent().block()

        then:
        verifyAll(newContent.posts[0]) {
            id == post.id
            image == post.image
            user == post.user
        }
    }

    def 'post info cannot be fetched'() {
        when:
        PostInfo info = pr0grammClient.getPostInfo(new Post()).block()

        then:
        noExceptionThrown()
        !info
    }

    def 'pending messages cannot be feched'() {
        when:
        List<Pr0User> users = pr0grammClient.getUserWithPendingMessages().collectList().block()

        then:
        noExceptionThrown()
        !users
    }

    def 'messages with user cannot be fetched'() {
        given:
        Pr0User user = new Pr0User(1, 'SomeDude')

        when:
        List<Message> messages = pr0grammClient.getMessagesWith(user).collectList().block()

        then:
        noExceptionThrown()
        !messages
    }

    def 'message cannot be posted'() {
        given:
        Pr0grammMessage pr0grammMessage = new Pr0grammMessage(1, 'SomeDude', 'Hello')

        when:
        pr0grammClient.sendNewMessage(pr0grammMessage).block()

        then:
        noExceptionThrown()
    }

    def 'comment cannot be posted'() {
        given:
        Pr0grammComment comment = new Pr0grammComment(1, 'Hello World')

        when:
        pr0grammClient.postNewComment(comment).block()

        then:
        noExceptionThrown()
    }

    def createNewContent(List<Post> posts) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/items/get')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/json')
                .withBody(objectMapper.writeValueAsString(new Content(posts: posts)))
        )
    }
}
