package com.pr0gramm.keycrawler.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.pr0gramm.keycrawler.api.*
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
import spock.lang.Shared
import spock.lang.Specification

import static org.mockserver.integration.ClientAndServer.startClientAndServer

@SpringBootTest(classes = [WebClientAutoConfiguration, JacksonAutoConfiguration, Pr0grammApiClientConfig, Nonce, Pr0grammClient])
class Pr0grammClientMockIT extends Specification {

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
    }

    def cleanup() {
        mockServerClient.reset()
    }

    def cleanupSpec() {
        mockServerClient.stop()
    }

    def 'new content is fetched correctly'() {
        given:
        Post post = new Post(id: 1, image: 'image/image1.jpg', user: 'TestUser')
        createNewContent([post])

        when:
        Content newContent = pr0grammClient.fetchNewContent(true).block()

        then:
        verifyAll(newContent.posts[0]) {
            id == post.id
            image == post.image
            user == post.user
        }
    }

    def 'post info is fetched correctly'() {
        given:
        Post post = new Post(id: 1)
        PostInfo postInfo = new PostInfo(
                tags: [new Tag(id: 2, name: 'Tag1'), new Tag(id: 3, name: 'Tag2')],
                comments: [new Comment(id: 4, parentId: 0, content: 'Hello World', created: System.currentTimeMillis(), up: 5, down: 1, userName: 'User1')])
        createPostInfo(post, postInfo)

        when:
        PostInfo info = pr0grammClient.getPostInfo(post).block()

        then:
        info == postInfo
    }

    def 'pending messages can be feched'() {
        given:
        Message pendingMessage = new Message(senderId: 100, userName: 'XMrNiceGuyX')
        createNewPendingMessages([pendingMessage])

        when:
        List<Pr0User> users = pr0grammClient.getUserWithPendingMessages().collectList().block()

        then:
        users.size() == 1
        users[0].userId == pendingMessage.senderId
        users[0].userName == pendingMessage.userName
    }

    def 'messages with user can be fetched'() {
        given:
        Pr0User user = new Pr0User(1, 'SomeDude')
        String message = 'HelloWorld'

        Message myMessage = new Message(userName: 'XMrNiceGuyX', message: 'Heyho')
        Message userMessage = new Message(userName: user.userName, message: message)
        createMessagesWithUser(user.userName, [myMessage, userMessage])

        when:
        List<Message> messages = pr0grammClient.getMessagesWith(user).collectList().block()

        then:
        !messages.empty
        verifyAll(messages[1]) {
            it.userName == userName
            it.message == message
        }
    }

    def 'message can be posted'() {
        given:
        Pr0grammMessage pr0grammMessage = new Pr0grammMessage(1, 'SomeDude', 'Hello')
        createSuccessfulMessagePost(pr0grammMessage)

        when:
        pr0grammClient.sendNewMessage(pr0grammMessage).block()

        then:
        noExceptionThrown()
    }

    def 'comment can be posted'() {
        given:
        Pr0grammComment comment = new Pr0grammComment(1, 'Hello World')
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
                .withBody(objectMapper.writeValueAsString(new Content(posts: posts)))
        )
    }

    def createPostInfo(Post post, PostInfo postInfo) {
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

    def createMessagesWithUser(String userName, List<Message> messages) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('GET')
                .withPath('/pr0gramm.com/api/inbox/messages')
                .withHeader('Accept', 'application/json')
                .withCookie('me', ME_COOKIE)
                .withQueryStringParameter('with', userName)
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader('Content-Type', 'application/json')
                .withBody(objectMapper.writeValueAsString(new Messages(messages: messages)))
        )
    }

    def createSuccessfulMessagePost(Pr0grammMessage message) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('POST')
                .withPath('/pr0gramm.com/api/inbox/post')
                .withHeader('Accept', 'application/json')
                .withHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8')
                .withCookie('me', ME_COOKIE)
                .withBody("recipientId=${message.userId}&comment=${message.message}&_nonce=${NONCE}")
        ).respond(HttpResponse.response()
                .withStatusCode(200)
        )
    }

    def createSuccessfulCommentPost(Pr0grammComment comment) {
        mockServerClient.when(HttpRequest.request()
                .withMethod('POST')
                .withPath('/pr0gramm.com/api/comments/post')
                .withHeader('Accept', 'application/json')
                .withHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8')
                .withCookie('me', ME_COOKIE)
                .withBody("itemId=${comment.postId}&parentId=0&comment=${comment.message}&_nonce=${NONCE}")
        ).respond(HttpResponse.response()
                .withStatusCode(200)
        )
    }
}
