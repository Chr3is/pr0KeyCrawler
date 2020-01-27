package com.pr0gramm.crawler.client

import com.pr0gramm.crawler.api.model.NewPr0Comment
import com.pr0gramm.crawler.api.model.NewPr0Message
import com.pr0gramm.crawler.model.Pr0User
import com.pr0gramm.crawler.model.client.Pr0Content
import com.pr0gramm.crawler.model.client.Pr0Messages
import com.pr0gramm.crawler.model.client.Pr0Post
import com.pr0gramm.crawler.model.client.Pr0PostInfo
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
        Pr0Content content = pr0grammClientUser1.fetchNewContent().block()

        then:
        noExceptionThrown()

        and:
        !content.posts.empty
    }

    def 'user1 can send a message to user2'() {
        given:
        NewPr0Message message = new NewPr0Message(new Pr0User(ID_USER_2, NAME_USER_2), MESSAGE_CONTENT)

        when:
        pr0grammClientUser1.sendNewMessage(message).block()

        then:
        noExceptionThrown()
    }

    def 'user2 can get the new pending message'() {
        when:
        Pr0Messages messages = pr0grammClientUser2.getPendingMessagesByUser().block()

        then:
        noExceptionThrown()

        and:
        messages.messages.size() == 1
        verifyAll(messages.messages[0]) {
            id == ID_USER_1
            userName == NAME_USER_1
        }
    }

    def 'user2 can mark the message as read'() {
        given:
        Pr0User pr0User = new Pr0User(ID_USER_1, NAME_USER_1)

        when:
        Pr0Messages messages = pr0grammClientUser2.getMessagesWith(pr0User).block()

        then:
        noExceptionThrown()

        and:
        !messages.messages.empty
        verifyAll(messages.messages[0]) {
            userName == NAME_USER_1
            getMessage() == MESSAGE_CONTENT
        }

        then:
        Pr0Messages newMessages = pr0grammClientUser2.getPendingMessagesByUser().block()
        newMessages.messages.empty
    }

    def 'user2 can post a comment under user1 post'() {
        given:
        Pr0Post post = new Pr0Post(id: TEST_POST_ID)
        NewPr0Comment commentToPost = new NewPr0Comment(post, MESSAGE_CONTENT)

        when:
        pr0grammClientUser2.postNewComment(commentToPost).block()

        then:
        noExceptionThrown()

        when:
        Pr0PostInfo postInfo = pr0grammClientUser2.getPostInfo(post).block()

        then:
        verifyAll(postInfo) {
            comments.find { comment -> comment.content == MESSAGE_CONTENT && comment.name == NAME_USER_2 }
        }
    }
}