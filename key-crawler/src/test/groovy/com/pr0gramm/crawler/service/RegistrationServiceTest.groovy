package com.pr0gramm.crawler.service


import com.pr0gramm.crawler.config.properties.RegistrationProperties
import com.pr0gramm.crawler.model.Pr0User
import com.pr0gramm.crawler.model.client.Pr0Message
import com.pr0gramm.crawler.repository.UserRepository
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
        }) >> Mono.just(new Pr0User(100, userName))
    }

    def 'user will only registered if he is not known'() {
        given:
        String username = '12345'
        Pr0User pr0User = new Pr0User(100, username)

        when:
        registrationService.registerNewUser(pr0User).block()

        then:
        1 * userRepository.getByUserName(username) >> Mono.just(new Pr0User(100, username))
        0 * userRepository.save(_)
    }

    def 'users can be registered by message with the correct keyword'() {
        given:
        Pr0User user1 = new Pr0User(100, 'a')
        Pr0User user2 = new Pr0User(101, 'b')
        Tuple2<Pr0User, List<Pr0Message>> messagesByUser1 = Tuples.of(user1, [new Pr0Message(), new Pr0Message(message: 'Test'), new Pr0Message(message: "Hello World ${REGISTRATION_KEY_WORD}")])
        Tuple2<Pr0User, List<Pr0Message>> messagesByUser2 = Tuples.of(user2, [new Pr0Message(message: "Hello World ${REGISTRATION_KEY_WORD}"), new Pr0Message(message: 'BLABLABLA')])

        when:
        registrationService.handleNewRegistrations().block()

        then:
        1 * pr0grammMessageService.checkForNewPendingMessages() >> Flux.just(messagesByUser1, messagesByUser2)
        2 * userRepository.getByUserName(_) >> Mono.empty()
        2 * userRepository.save({
            it.userName == messagesByUser1.t1.name || it.userName == user2.name && it.token && it.proUserId == user1.id || it.proUserId == user2.id
        }) >> { Mono.just(it[0]) }
        2 * pr0grammMessageService.markMessagesAsReadFor(_) >> Mono.empty()
        2 * pr0grammMessageService.sendNewMessage(_) >> Mono.empty()
    }

    def 'users will not be registered by message if keyword is missing'() {
        given:
        Pr0User user1 = new Pr0User(100, 'a')
        Pr0User user2 = new Pr0User(101, 'b')
        Tuple2<Pr0User, List<Pr0Message>> messagesByUser1 = Tuples.of(user1, [new Pr0Message(), new Pr0Message(message: 'Test')])
        Tuple2<Pr0User, List<Pr0Message>> messagesByUser2 = Tuples.of(user2, [new Pr0Message(message: 'BLABLABLA')])

        when:
        registrationService.handleNewRegistrations().block()

        then:
        1 * pr0grammMessageService.checkForNewPendingMessages() >> Flux.just(messagesByUser1, messagesByUser2)
        0 * userRepository.getByUserName(_) >> Mono.empty()
        0 * userRepository.save(_)
        0 * pr0grammMessageService.sendNewMessage(_) >> Mono.empty()
    }
}
