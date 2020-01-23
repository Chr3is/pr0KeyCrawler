package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Message
import com.pr0gramm.keycrawler.config.properties.RegistrationProperties
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.repository.User
import com.pr0gramm.keycrawler.repository.UserRepository
import com.pr0gramm.keycrawler.service.exception.AuthenticationFailedException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject

class UserServiceTest extends Specification {

    static final long CHAT_ID = 1
    static final String USER_NAME = '12345'
    static final String TOKEN = 'abcdef'
    static final REGISTRATION_KEY_WORD = 'pr0KeyCrawler'
    RegistrationProperties properties = new RegistrationProperties(enabled: true, keyWord: REGISTRATION_KEY_WORD)

    UserRepository userRepository = Mock()

    Pr0grammMessageService pr0grammMessageService = Mock()

    @Subject
    UserService userService = new UserService(properties, userRepository, pr0grammMessageService)

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

    def 'registration through pr0gramm is not executed if not enabled'() {
        given:
        UserService userService = new UserService(new RegistrationProperties(enabled: false), userRepository, pr0grammMessageService)

        when:
        userService.handleNewRegistrations().block()

        then:
        0 * pr0grammMessageService.getPendingMessages()
        0 * userService.registerNewUser(_)
        0 * pr0grammMessageService.markMessagesAsReadFor(_)
    }

    def 'direct registration is still possible if registration is disabled'() {
        given:
        Pr0User user = new Pr0User(100, 'a')
        UserService userService = new UserService(new RegistrationProperties(enabled: false), userRepository, pr0grammMessageService)

        when:
        userService.registerNewUser(user).block()

        then:
        1 * userRepository.getByUserName(_) >> Mono.empty()
        1 * userRepository.save(_) >> { Mono.just(it[0]) }
    }

    def 'users can be registered by message with the correct keyword'() {
        given:
        Pr0User user1 = new Pr0User(100, 'a')
        Pr0User user2 = new Pr0User(101, 'b')
        Tuple2<Pr0User, List<Message>> messagesByUser1 = Tuples.of(user1, [new Message(), new Message(message: 'Test'), new Message(message: "Hello World ${REGISTRATION_KEY_WORD}")])
        Tuple2<Pr0User, List<Message>> messagesByUser2 = Tuples.of(user2, [new Message(message: "Hello World ${REGISTRATION_KEY_WORD}"), new Message(message: 'BLABLABLA')])

        when:
        userService.handleNewRegistrations().block()

        then:
        1 * pr0grammMessageService.getPendingMessages() >> Flux.just(messagesByUser1, messagesByUser2)
        2 * userRepository.getByUserName(_) >> Mono.empty()
        2 * userRepository.save({
            it.userName == messagesByUser1.t1.userName || it.userName == user2.userName && it.token && it.proUserId == user1.userId || it.proUserId == user2.userId
        }) >> { Mono.just(it[0]) }
        2 * pr0grammMessageService.markMessagesAsReadFor(_) >> Mono.empty()
        2 * pr0grammMessageService.sendNewMessage(_) >> Mono.empty()
    }

    def 'users will not be registered by message if keyword is missing'() {
        given:
        Pr0User user1 = new Pr0User(100, 'a')
        Pr0User user2 = new Pr0User(101, 'b')
        Tuple2<Pr0User, List<Message>> messagesByUser1 = Tuples.of(user1, [new Message(), new Message(message: 'Test')])
        Tuple2<Pr0User, List<Message>> messagesByUser2 = Tuples.of(user2, [new Message(message: 'BLABLABLA')])

        when:
        userService.handleNewRegistrations().block()

        then:
        1 * pr0grammMessageService.getPendingMessages() >> Flux.just(messagesByUser1, messagesByUser2)
        0 * userRepository.getByUserName(_) >> Mono.empty()
        0 * userRepository.save(_)
        0 * pr0grammMessageService.sendNewMessage(_) >> Mono.empty()
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
