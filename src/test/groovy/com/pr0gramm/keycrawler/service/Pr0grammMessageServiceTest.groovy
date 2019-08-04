package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Message
import com.pr0gramm.keycrawler.client.Pr0grammClient
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.model.Pr0grammMessage
import com.pr0gramm.keycrawler.repository.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class Pr0grammMessageServiceTest extends Specification {

    Pr0grammClient pr0grammClient = Mock()

    Pr0grammMessageService pr0grammMessageService = new Pr0grammMessageService(pr0grammClient)

    def 'message will be marked as read when fetching pending messages'() {
        given:
        Pr0User user1 = new Pr0User(1, 'first')
        Pr0User user2 = new Pr0User(2, 'second')

        when:
        List<Pr0User> userWithPendingMsgs = pr0grammMessageService.getUsersWithPendingMessages().collectList().block()

        then:
        userWithPendingMsgs.size() == 2
        userWithPendingMsgs[0] == user1
        userWithPendingMsgs[1] == user2

        then:
        1 * pr0grammClient.getUserWithPendingMessages() >> Flux.just(user1, user2)
        2 * pr0grammClient.getMessagesWith(_) >> Flux.just(new Message())
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