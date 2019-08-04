package com.pr0gramm.keycrawler.repository


import com.pr0gramm.keycrawler.repository.User
import com.pr0gramm.keycrawler.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Stepwise

@SpringBootTest
@Stepwise
class UserRepositoryIT extends Specification {

    static final String USER_NAME = 'testUser'
    static final long CHAT_ID = 12345

    @Autowired
    UserRepository userRepository

    def 'database is empty'() {
        when:
        userRepository.deleteAll().block()

        then:
        userRepository.count().block() == 0
    }

    def 'user can be added'() {
        given:
        User user = new User(100, USER_NAME, UUID.randomUUID().toString())

        when:
        userRepository.save(user).block()

        then:
        User result = userRepository.getByUserName(USER_NAME).block()
        verifyAll(result) {
            id
            proUserId == 100
            userName == USER_NAME
            token
            !chatId
            !verified
            subscribed
        }
    }

    def 'second user without a pr0 user id can be added'() {
        given:
        String name = 'notAnPr0Acc'
        User user = new User(null, name, UUID.randomUUID().toString())

        when:
        userRepository.save(user).block()

        then:
        User result = userRepository.getByUserName(name).block()
        verifyAll(result) {
            id
            !proUserId
            userName == name
            token
            !chatId
            !verified
            subscribed
        }
    }

    def 'user can be updated'() {
        when:
        User user = userRepository.getByUserName(USER_NAME).block()

        then:
        user.setVerified(true)
        user.setChatId(CHAT_ID)
        userRepository.save(user).block()

        when:
        User result = userRepository.getByUserName(USER_NAME).block()

        then:
        verifyAll(result) {
            id
            userName == USER_NAME
            token
            chatId == CHAT_ID
            verified
            subscribed
        }
    }

    def 'user can be found by chatId'() {
        when:
        User result = userRepository.getByChatId(CHAT_ID).block()

        then:
        verifyAll(result) {
            id
            userName == USER_NAME
            token
            chatId == CHAT_ID
            verified
            subscribed
        }
    }

    def 'subscribed and verified users are returned'() {
        given:
        User notVerified = new User(null, 100, 'notVerified', 'token1', null, false, true)
        User notSubscribed = new User(null, 101, 'notSubscribed', 'token2', null, true, false)

        when:
        userRepository.saveAll([notVerified, notSubscribed]).blockLast()

        then:
        userRepository.count().block() == 4

        when:
        List<User> result = userRepository.getAllByStatusAndSubscribed(true, true).collectList().block()

        then:
        result.size() == 1
    }

    def 'user can be deleted by chatId'() {
        when:
        userRepository.deleteByChatId(CHAT_ID).block()

        then:
        userRepository.count().block() == 3
    }

    def 'remaining accounts can be deleted'() {
        when:
        userRepository.deleteAll().block()

        then:
        userRepository.count().block() == 0
    }

}
