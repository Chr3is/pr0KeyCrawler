package com.pr0gramm.keycrawler.service


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

    @Subject
    UserService userService = new UserService(userRepository)

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
