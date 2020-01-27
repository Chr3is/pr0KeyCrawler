package com.pr0gramm.crawler.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.pr0gramm.crawler.api.model.NewPr0Comment
import com.pr0gramm.crawler.api.model.NewPr0Message
import com.pr0gramm.crawler.client.api.*
import com.pr0gramm.crawler.config.MappingsConfig
import com.pr0gramm.crawler.config.Pr0grammApiClientConfig
import com.pr0gramm.crawler.model.Nonce
import com.pr0gramm.crawler.model.Pr0User
import com.pr0gramm.crawler.model.client.Pr0Content
import com.pr0gramm.crawler.model.client.Pr0Message
import com.pr0gramm.crawler.model.client.Pr0Post
import com.pr0gramm.crawler.model.client.Pr0PostInfo
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

@SpringBootTest(classes = [WebClientAutoConfiguration, JacksonAutoConfiguration, Pr0grammApiClientConfig, Nonce, MappingsConfig])
@TestPropertySource(properties = [
        'pr0gramm.api-client.url=http://localhost:27119/pr0gramm.com/',
        'pr0gramm.api-client.cookies[me]=%7B%22n%22%3A%22Test%22%2C%22id%22%3A%22123456789abcdefghijk%22%7D'])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class Pr0grammClientMockAuthenticatedIT extends Specification {

    static final String ME_COOKIE = '%7B%22n%22%3A%22Test%22%2C%22id%22%3A%22123456789abcdefghijk%22%7D'
    static final String NONCE = '123456789abcdefg'

    @Shared
    MockServerClient mockServerClient

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    Pr0grammClient pr0grammClient

    def setupSpec() {
        mockServerClient = startClientAndServer(27119)
        // The Pr0grammClient will execute a request while being configure to check if it can be use as authenticated
        // We have to response to this with request with this mocked response (200 leads to an authenticated client)
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/items/get')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
                .withQueryStringParameter('flags', '15')
        ).respond(HttpResponse.response()
                .withStatusCode(200)
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

    def 'client is authenticated'() {
        expect:
        pr0grammClient.metaPropertyValues[0].bean.isAuthenticated
    }

    def 'new content is fetched correctly'() {
        given:
        Post post = new Post(id: 1, image: 'image/image1.jpg', user: 'TestUser')
        createNewContent([post])

        when:
        Pr0Content newContent = pr0grammClient.fetchNewContent().block()

        then:
        verifyAll(newContent.posts[0]) {
            id == post.id
            contentLink == post.image
            user == post.user
        }
    }

    def 'post info is fetched correctly'() {
        given:
        Pr0Post pr0Post = new Pr0Post(id: 1)
        PostInfo postInfo = new PostInfo(
                tags: [new Tag(id: 2, tag: 'Tag1'), new Tag(id: 3, tag: 'Tag2')],
                comments: [new Comment(id: 4, parent: 0, content: 'Hello World', created: System.currentTimeMillis(), up: 5, down: 1, name: 'User1')])
        createPostInfo(pr0Post, postInfo)

        when:
        Pr0PostInfo info = pr0grammClient.getPostInfo(pr0Post).block()

        then:
        verifyAll(info) {
            verifyAll(tags) {
                size() == 2
                it[0].id == postInfo.tags[0].id
                it[0].name == postInfo.tags[0].tag
                it[1].id == postInfo.tags[1].id
                it[1].name == postInfo.tags[1].tag
            }
            verifyAll(comments) {
                size() == 1
                verifyAll(it[0]) {
                    id == postInfo.comments[0].id
                    parent == postInfo.comments[0].parent
                    userName == postInfo.comments[0].name
                    content == postInfo.comments[0].content
                    created == postInfo.comments[0].created
                    up == postInfo.comments[0].up
                    down == postInfo.comments[0].down
                }
            }
        }

    }

    def 'pending messages can be feched'() {
        given:
        Message pendingMessage = new Message(senderId: 100, name: 'XMrNiceGuyX', message: 'Message1')
        Message pendingMessage2 = new Message(senderId: 101, name: 'SomeOtherUser', message: 'Message3')
        createNewPendingMessages([pendingMessage, pendingMessage2])

        when:
        List<Pr0Message> pendingMessagesByUser = pr0grammClient.getPendingMessagesByUser().block().getMessages()

        then:
        pendingMessagesByUser.size() == 2
        verifyAll(pendingMessagesByUser[0]) {
            userName == pendingMessage.name
            senderId == pendingMessage.senderId
            message == pendingMessage.message
        }

        verifyAll(pendingMessagesByUser[1]) {
            userName == pendingMessage2.name
            senderId == pendingMessage2.senderId
            message == pendingMessage2.message
        }
    }

    def 'messages with user can be fetched'() {
        given:
        Pr0User user = new Pr0User(1, 'SomeDude')
        String message = 'HelloWorld'

        Message myMessage = new Message(name: 'XMrNiceGuyX', message: 'Heyho')
        Message userMessage = new Message(name: user.name, message: message)
        createMessagesWithUser(user, [myMessage, userMessage])

        when:
        List<Pr0Message> messages = pr0grammClient.getMessagesWith(user).block().getMessages()

        then:
        !messages.empty
        verifyAll(messages[1]) {
            it.userName == user.name
            it.message == message
        }
    }

    def 'message can be posted'() {
        given:
        NewPr0Message pr0grammMessage = new NewPr0Message(new Pr0User(1, 'SomeDude'), 'Hello')
        createSuccessfulMessagePost(pr0grammMessage)

        when:
        pr0grammClient.sendNewMessage(pr0grammMessage).block()

        then:
        noExceptionThrown()
    }

    def 'comment can be posted'() {
        given:
        NewPr0Comment comment = new NewPr0Comment(new Pr0Post(id: 1), 'Hello World')
        createSuccessfulCommentPost(comment)

        when:
        pr0grammClient.postNewComment(comment)

        then:
        noExceptionThrown()
    }

    def createNewContent(List<Post> posts) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/items/get')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
                .withQueryStringParameter('flags', '15')
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/json')
                .withBody(objectMapper.writeValueAsString(new Content(items: posts)))
        )
    }

    def createPostInfo(Pr0Post post, PostInfo postInfo) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/items/info')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
                .withQueryStringParameter('itemId', '' + post.id)
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/json')
                .withBody(objectMapper.writeValueAsString(postInfo)))
    }

    def createNewPendingMessages(List<Message> messages) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/inbox/pending')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/json')
                .withBody(objectMapper.writeValueAsString(new Messages(messages: messages)))
        )
    }

    def createMessagesWithUser(Pr0User user, List<Message> messages) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/inbox/messages')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
                .withQueryStringParameter('with', user.name)
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/json')
                .withBody(objectMapper.writeValueAsString(new Messages(messages: messages)))
        )
    }

    def createSuccessfulMessagePost(NewPr0Message message) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('POST')
                .withPath('/pr0gramm.com/api/inbox/post')
                .withHeader('Accept', 'application/json')
                .withHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8')
                .withCookie('me', ME_COOKIE)
                .withBody("recipientId=${message.recipient.name}&comment=${message.message}&_nonce=${NONCE}")
        ).respond(HttpResponse.response()
                .withStatusCode(200)
        )
    }

    def createSuccessfulCommentPost(NewPr0Comment comment) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('POST')
                .withPath('/pr0gramm.com/api/comments/post')
                .withHeader('Accept', 'application/json')
                .withHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8')
                .withCookie('me', ME_COOKIE)
                .withBody("itemId=${comment.post.id}&parentId=0&comment=${comment.message}&_nonce=${NONCE}")
        ).respond(HttpResponse.response()
                .withStatusCode(200)
        )
    }
}
