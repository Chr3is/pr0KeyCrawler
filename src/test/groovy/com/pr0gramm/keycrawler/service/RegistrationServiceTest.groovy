package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Message
import com.pr0gramm.keycrawler.config.properties.RegistrationProperties
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.repository.User
import com.pr0gramm.keycrawler.repository.UserRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject

class RegistrationServiceTest extends Specification {

    static final REGISTRATION_KEY_WORD = 'pr0KeyCrawler'

    RegistrationProperties properties = new RegistrationProperties(enabled: true, keyWord: REGISTRATION_KEY_WORD)

    UserRepository userRepository = Mock()

    Pr0grammMessageService pr0grammMessageService = Mock()

    @Subject
    RegistrationService registrationService = new RegistrationService(properties, userRepository, pr0grammMessageService)

    def 'users are registered and saved with a random token'() {
        given:
        String userName = '12345'
        Pr0User pr0User = new Pr0User(100, userName)

        when:
        registrationService.registerNewUser(pr0User).block()

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
        registrationService.registerNewUser(pr0User).block()

        then:
        1 * userRepository.getByUserName(username) >> Mono.just(new User(100, username, '1235'))
        0 * userRepository.save(_)
    }

    def 'users can be registered by message with the correct keyword'() {
        given:
        Pr0User user1 = new Pr0User(100, 'a')
        Pr0User user2 = new Pr0User(101, 'b')
        Tuple2<Pr0User, List<Message>> messagesByUser1 = Tuples.of(user1, [new Message(), new Message(message: 'Test'), new Message(message: "Hello World ${REGISTRATION_KEY_WORD}")])
        Tuple2<Pr0User, List<Message>> messagesByUser2 = Tuples.of(user2, [new Message(message: "Hello World ${REGISTRATION_KEY_WORD}"), new Message(message: 'BLABLABLA')])

        when:
        registrationService.handleNewRegistrations().block()

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
        registrationService.handleNewRegistrations().block()

        then:
        1 * pr0grammMessageService.getPendingMessages() >> Flux.just(messagesByUser1, messagesByUser2)
        0 * userRepository.getByUserName(_) >> Mono.empty()
        0 * userRepository.save(_)
        0 * pr0grammMessageService.sendNewMessage(_) >> Mono.empty()
    }
}
