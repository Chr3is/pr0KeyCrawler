package com.pr0gramm.keycrawler.client

import com.pr0gramm.keycrawler.api.Content
import com.pr0gramm.keycrawler.api.Message
import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.api.PostInfo
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.model.Pr0grammComment
import com.pr0gramm.keycrawler.model.Pr0grammMessage
import spock.lang.IgnoreIf
import spock.lang.Stepwise

/**
 * This test is only executed if two pr0gramm accounts are configured as system properties
 * First one: pr0.crawler.user1=userId:meCookie
 * Second one: pr0.crawler.user2=userId:meCookie
 */
@Stepwise
@IgnoreIf({ BaseIT.skipTest() })
class Pr0grammClientIT extends BaseIT {

    static final String MESSAGE_CONTENT = "IT TEST at ${System.currentTimeMillis()}"

    static final long TEST_POST_ID = 474409

    def 'user1 can get new content'() {
        when:
        Content content = pr0grammClientUser1.fetchNewContent().block()

        then:
        noExceptionThrown()

        and:
        !content.posts.empty
    }

    def 'user1 can send a message to user2'() {
        given:
        Pr0grammMessage message = new Pr0grammMessage(ID_USER_2, NAME_USER_2, MESSAGE_CONTENT)

        when:
        pr0grammClientUser1.sendNewMessage(message).block()

        then:
        noExceptionThrown()
    }

    def 'user2 can get the new pending message'() {
        when:
        List<Pr0User> pr0Users = pr0grammClientUser2.getPendingMessagesByUser().collectList().block()

        then:
        noExceptionThrown()

        and:
        pr0Users.size() == 1
        verifyAll(pr0Users[0]) {
            userId == ID_USER_1
            userName == NAME_USER_1
        }
    }

    def 'user2 can mark the message as read'() {
        given:
        Pr0User pr0User = new Pr0User(ID_USER_1, NAME_USER_1)

        when:
        List<Message> messages = pr0grammClientUser2.getMessagesWith(pr0User).collectList().block()

        then:
        noExceptionThrown()

        and:
        !messages.empty
        verifyAll(messages[0]) {
            userName == NAME_USER_1
            getMessage() == MESSAGE_CONTENT
        }

        then:
        List<Pr0User> pendingUsers = pr0grammClientUser2.getPendingMessagesByUser().collectList().block()
        pendingUsers.empty
    }

    def 'user2 can post a comment under user1 post'() {
        given:
        Post post = new Post(id: TEST_POST_ID)
        Pr0grammComment commentToPost = new Pr0grammComment(post.id, MESSAGE_CONTENT)

        when:
        pr0grammClientUser2.postNewComment(commentToPost).block()

        then:
        noExceptionThrown()

        when:
        PostInfo postInfo = pr0grammClientUser2.getPostInfo(post).block()

        then:
        verifyAll(postInfo) {
            comments.find { comment -> comment.content == MESSAGE_CONTENT && comment.userName == NAME_USER_2 }
        }
    }
}