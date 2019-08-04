package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.repository.User
import com.pr0gramm.keycrawler.repository.UserRepository
import com.pr0gramm.keycrawler.service.exception.AuthenticationFailedException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

class UserServiceTest extends Specification {

    static final long CHAT_ID = 1
    static final String USER_NAME = '12345'
    static final String TOKEN = 'abcdef'

    UserRepository userRepository = Mock()

    Pr0grammMessageService pr0grammMessageService = Mock()

    @Subject
    UserService userService = new UserService(userRepository, pr0grammMessageService)

    def 'only verified and subscribed users are returned'() {
        given:
        User verifiedAndSubscribedUser = new User(1, 100, 'verifiedAndSubscribed', '1', null, true, true)

        when:
        List<User> users = userService.getAllVerifiedAndSubscribedUsers().collectList().block()

        then:
        users.size() == 1

        verifyAll(users.get(0)) {
            proUserId == verifiedAndSubscribedUser.proUserId
            userName == verifiedAndSubscribedUser.userName
            token == verifiedAndSubscribedUser.token
            verified == verifiedAndSubscribedUser.verified
            !chatId
        }

        and:
        1 * userRepository.getAllByStatusAndSubscribed(true, true) >> Flux.just(verifiedAndSubscribedUser)
    }

    def 'users are registered and saved with a random token'() {
        given:
        String userName = '12345'
        Pr0User pr0User = new Pr0User(100, userName)

        when:
        userService.registerNewUser(pr0User).block()

        then:
        1 * userRepository.getByUserName(userName) >> Mono.empty()
        1 * userRepository.save({
            it.proUserId == 100 && it.userName == userName && it.token && !it.chatId && !it.verified && it.subscribed
        }) >> Mono.just(new User(100, userName, 'abc'))
    }

    def 'user will only registered if he is not known'() {
        given:
        String username = '12345'
        Pr0User pr0User = new Pr0User(100, username)

        when:
        userService.registerNewUser(pr0User).block()

        then:
        1 * userRepository.getByUserName(username) >> Mono.just(new User(100, username, '1235'))
        0 * userRepository.save(_)
    }

    def 'users can be registered by message'() {
        given:
        Pr0User user1 = new Pr0User(100, 'a')
        Pr0User user2 = new Pr0User(101, 'b')

        when:
        userService.handleNewRegistrations().collectList().block()

        then:
        1 * pr0grammMessageService.getUsersWithPendingMessages() >> Flux.just(user1, user2)
        2 * userRepository.getByUserName(_) >> Mono.empty()
        2 * userRepository.save({
            it.userName == user1.userName || it.userName == user2.userName && it.token && it.proUserId == user1.userId || it.proUserId == user2.userId
        }) >> { Mono.just(it[0]) }
        2 * pr0grammMessageService.sendNewMessage(_) >> Mono.empty()
    }

    def 'users can be authenticated'() {
        when:
        boolean authenticated = userService.authenticateUser(CHAT_ID, USER_NAME, TOKEN).block()

        then:
        authenticated

        and:
        1 * userRepository.getByUserName(USER_NAME) >> Mono.just(new User(1, 100, USER_NAME, TOKEN, null, false, true))
        1 * userRepository.save({
            it.proUserId == 100 && it.userName == USER_NAME && it.token == TOKEN && it.chatId == CHAT_ID && it.verified && it.subscribed
        }) >> { Mono.just(it[0]) }
    }

    def 'users with wrong token wont be authenticated'() {
        when:
        userService.authenticateUser(CHAT_ID, USER_NAME, TOKEN).block()

        then:
        thrown(AuthenticationFailedException)

        and:
        1 * userRepository.getByUserName(USER_NAME) >> Mono.just(new User(1, 100, USER_NAME, 'wrong', null, false, true))
        0 * userRepository.save(*_)
    }

    def 'subscription can be added'() {
        when:
        boolean result = userService.subscribeUser(CHAT_ID).block()

        then:
        result

        and:
        1 * userRepository.getByChatId(CHAT_ID) >> Mono.just(new User(1, 100, USER_NAME, TOKEN, CHAT_ID, true, false))
        1 * userRepository.save({
            it.proUserId == 100 && it.userName == USER_NAME && it.token == TOKEN && it.chatId == CHAT_ID && it.verified && it.subscribed
        }) >> { Mono.just(it[0]) }
    }

    def 'subscription can be removed'() {
        when:
        boolean result = userService.unsubscribeUser(CHAT_ID).block()

        then:
        result

        and:
        1 * userRepository.getByChatId(CHAT_ID) >> Mono.just(new User(1, 100, USER_NAME, TOKEN, CHAT_ID, true, true))
        1 * userRepository.save({
            it.proUserId == 100 && it.userName == USER_NAME && it.token == TOKEN && it.chatId == CHAT_ID && it.verified && !it.subscribed
        }) >> { Mono.just(it[0]) }
    }

    def 'user can be deleted'() {
        when:
        userService.deleteUser(CHAT_ID).block()

        then:
        1 * userRepository.deleteByChatId(CHAT_ID) >> Mono.empty()
    }
}
