package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Message
import com.pr0gramm.keycrawler.api.Messages
import com.pr0gramm.keycrawler.client.Pr0grammClient
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.model.Pr0grammMessage
import com.pr0gramm.keycrawler.repository.User
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import spock.lang.Specification

class Pr0grammMessageServiceTest extends Specification {

    Pr0grammClient pr0grammClient = Mock()

    Pr0grammMessageService pr0grammMessageService = new Pr0grammMessageService(pr0grammClient)

    def 'message will be marked as read when fetching pending messages'() {
        given:
        Message pendingMessage = new Message(senderId: 100, userName: 'XMrNiceGuyX', message: 'Message1')
        Message pendingMessage2 = new Message(senderId: 100, userName: 'XMrNiceGuyX', message: 'Message2')
        Message pendingMessage3 = new Message(senderId: 101, userName: 'SomeOtherUser', message: 'Message3')

        when:
        List<Tuple2<Pr0User, List<Message>>> pendingMessagesByUser = pr0grammMessageService.getPendingMessages().collectList().block()

        then:
        pendingMessagesByUser.size() == 2
        verifyAll(pendingMessagesByUser) {
            verifyAll(it[0]) {
                verifyAll(t1) {
                    userId == pendingMessage.senderId
                    userId == pendingMessage2.senderId
                    userName == pendingMessage.userName
                    userName == pendingMessage2.userName
                }
                verifyAll(t2) {
                    verifyAll(t2[0]) {
                        senderId == pendingMessage.senderId
                        userName == pendingMessage.userName
                        message == pendingMessage.message
                    }
                    verifyAll(t2[1]) {
                        senderId == pendingMessage2.senderId
                        userName == pendingMessage2.userName
                        message == pendingMessage2.message
                    }
                }
            }
            verifyAll(it[1]) {
                verifyAll(t1) {
                    userId == pendingMessage3.senderId
                    userName == pendingMessage3.userName
                }
                verifyAll(t2) {
                    verifyAll(t2[0]) {
                        senderId == pendingMessage3.senderId
                        userName == pendingMessage3.userName
                        message == pendingMessage3.message
                    }
                }
            }
        }

        then:
        1 * pr0grammClient.getPendingMessagesByUser() >> Mono.just(new Messages(messages: [pendingMessage, pendingMessage2, pendingMessage3]))
    }

    def 'messages can be marked as read'() {
        given:
        User user = new User(1, 'User1', '')

        when:
        pr0grammMessageService.markMessagesAsReadFor(user).block()

        then:
        1 * pr0grammClient.getMessagesWith(user.userName) >> Mono.empty()
    }

    def 'message will be send'() {
        when:
        pr0grammMessageService.sendNewMessage(new User(1, 'User', 'abc')).block()

        then:
        1 * pr0grammClient.sendNewMessage({ it.message.contains('User:abc') }) >> Mono.empty()
    }

    def 'message is created correctly'() {
        given:
        String userName = 'test'
        String token = 'token'
        long userId = 1
        User user = new User(userId, userName, token)

        when:
        Pr0grammMessage message = pr0grammMessageService.createMessage(user)

        then:
        message.userId == userId
        message.userName == userName
        message.message == "Thanks for your registration!\nYour key is: $userName:$token\nCopy the string above and reply with it to the Telegram Bot!"
    }

}