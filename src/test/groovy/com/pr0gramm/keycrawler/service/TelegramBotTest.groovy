package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.config.properties.TelegramProperties
import com.pr0gramm.keycrawler.model.KeyResult
import com.pr0gramm.keycrawler.model.Pr0User
import com.pr0gramm.keycrawler.repository.User
import com.pr0gramm.keycrawler.service.telegram.TelegramBot
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class TelegramBotTest extends Specification {

    UserService userService = Mock()
    SilentSender silent = Mock()

    @Subject
    TelegramBot telegramBot = new TelegramBot(userService, new TelegramProperties())

    def setup() {
        telegramBot.silent = silent
    }

    def 'no keys result in no sent messages'() {
        when:
        telegramBot.sendMessage([])

        then:
        0 * userService._
        0 * silent._
    }

    @Unroll
    def 'results=#results will be send as message=#message'(List<KeyResult> results, String message) {
        given:
        long chatId = 1

        when:
        telegramBot.sendMessage(results).block()

        then:
        1 * userService.getAllVerifiedAndSubscribedUsers() >> Flux.just(new User(chatId: chatId))
        1 * silent.send(message, chatId) >> Optional.empty()

        where:
        results                                                                                                    || message
        [new KeyResult(Tuples.of(new Post(user: 'Test', fullUrl: 'abc'), 'nothing'))]                              || 'Say thanks to: Test. The post can be found here: abc0\n'
        [new KeyResult(Tuples.of(new Post(user: 'Test', fullUrl: 'abc'), '8NONA-M7B7W-WB2JT'))]                    || 'Say thanks to: Test. The post can be found here: abc0\n8NONA-M7B7W-WB2JT\n'
        [new KeyResult(Tuples.of(new Post(user: 'Test', fullUrl: 'abc'), '8NONA-M7B7W-WB2JT\n8NONA-M7B7W-WB2JT'))] || 'Say thanks to: Test. The post can be found here: abc0\n8NONA-M7B7W-WB2JT\n8NONA-M7B7W-WB2JT\n'
        [new KeyResult(Tuples.of(new Post(user: 'Test', fullUrl: 'abc'), '8NONA-M7B7W-WB2JT')),
         new KeyResult(Tuples.of(new Post(user: 'Test2', fullUrl: 'abc'), '8NONA-M7B7W-WB2JT'))]                   || 'Say thanks to: Test. The post can be found here: abc0\n8NONA-M7B7W-WB2JT\nSay thanks to: Test2. The post can be found here: abc0\n8NONA-M7B7W-WB2JT\n'

    }

    @Unroll
    def 'user can be authenticated with replyMsg=#replyMessage'(String replyMessage) {
        given:
        long chatId = 1
        String userName = 'test'
        String token = 'token'

        Update update = Stub()
        Message message = buildCorrectRepliedMessage(userName, token, chatId, replyMessage)
        update.getMessage() >> message

        String messageToSend = 'The authentication was successful. You will receive crawled keys in the future'

        when:
        telegramBot.authenticateAccount(update)

        then:
        1 * userService.authenticateUser(chatId, userName, token) >> Mono.just(new User())
        1 * silent.send(messageToSend, chatId) >> Optional.empty()

        where:
        replyMessage << ['Reply with the string you received as pr0gramm message',
                         'Authentication failed. Try it again with the string you received as pr0gramm message',
                         'Something is wrong with your syntax']
    }

    def 'user cant be authenticated'() {
        given:
        long chatId = 1
        String userName = 'test'
        String token = 'token'

        Update update = Stub()
        Message message = buildCorrectRepliedMessage(userName, token, chatId)
        update.getMessage() >> message

        String replyMessage = 'Authentication failed. Try it again with the string you received as pr0gramm message'

        when:
        telegramBot.authenticateAccount(update)

        then:
        1 * userService.authenticateUser(chatId, userName, token) >> Mono.error(new RuntimeException("Failed"))
        1 * silent.forceReply(replyMessage, chatId) >> Optional.empty()
    }

    def 'user uses wrong syntax'() {
        given:
        long chatId = 1
        String userName = 'test'
        String token = 'token'

        Update update = Stub()
        Message message = buildWithWrongSyntaxRepliedMessage(userName, token, chatId)
        update.getMessage() >> message

        String messageToSend = 'Something is wrong with your syntax'

        when:
        telegramBot.authenticateAccount(update)

        then:
        0 * userService.authenticateUser(*_)
        1 * silent.forceReply(messageToSend, chatId) >> Optional.empty()
    }

    @Unroll
    def 'user can be added without pr0=#result and message=#message is sent to user'(boolean result, String message) {
        given:
        String userName = 'test'
        String token = 'token'
        long chatId = 1
        MessageContext messageContext = Mock()

        when:
        telegramBot.addUserWithoutPr0gramm(messageContext)

        then:
        1 * messageContext.firstArg() >> userName
        1 * messageContext.chatId() >> chatId
        1 * userService.registerNewUser(new Pr0User(null, userName)) >> (result ? Mono.just(new User(null, userName, token)) : Mono.error(new RuntimeException("Failed")))
        1 * silent.send(message, chatId) >> Optional.empty()

        where:
        result || message
        true   || 'Success! The created token is: token'
        false  || 'Failed to add user with name: test'
    }

    @Unroll
    def 'user can be subscribed=#result and message=#message is sent to user'(boolean result, String message) {
        given:
        long chatId = 1
        MessageContext messageContext = MessageContext.newContext(null, null, chatId)

        when:
        telegramBot.subscribe(messageContext)

        then:
        1 * userService.subscribeUser(chatId) >> (result ? Mono.just(new User()) : Mono.error(new RuntimeException("Failed")))
        1 * silent.send(message, chatId) >> Optional.empty()

        where:
        result || message
        true   || 'Subscription was successful'
        false  || 'Subscription was not successful'
    }

    @Unroll
    def 'user can be unsubscribed=#result and message=#message is sent to user'(boolean result, String message) {
        given:
        long chatId = 1
        MessageContext messageContext = MessageContext.newContext(null, null, chatId)

        when:
        telegramBot.unsubscribe(messageContext)

        then:
        1 * userService.unsubscribeUser(chatId) >> (result ? Mono.just(new User()) : Mono.error(new RuntimeException('Failed')))
        1 * silent.send(message, chatId) >> Optional.empty()

        where:
        result || message
        true   || 'Unsubscription was successful'
        false  || 'Unsubscription was not successful'
    }

    @Unroll
    def 'user gets deleted=#result and message=#message is sent to user'(boolean result, String message) {
        given:
        long chatId = 1
        MessageContext messageContext = MessageContext.newContext(null, null, chatId)

        when:
        telegramBot.deleteAccount(messageContext)

        then:
        1 * userService.deleteUser(chatId) >> (result ? Mono.just(result) : Mono.error(new RuntimeException('Failed')))
        1 * silent.send(message, chatId) >> Optional.empty()

        where:
        result || message
        true   || 'Deletion was successful'
        false  || 'Deletion was not successful'
    }

    Message buildWithWrongSyntaxRepliedMessage(String userName, String token, long id) {
        Message message = buildCorrectRepliedMessage(userName, token, id)
        message.text = "$userName-----$token"
        return message
    }

    Message buildCorrectRepliedMessage(String userName, String token, long id, String replyText = 'Reply with the string you received as pr0gramm message') {
        Chat chat = new Chat(id: id)
        Message rplyMsg = new Message(text: replyText)
        Message msg = new Message(chat: chat, text: "$userName:$token", replyToMessage: rplyMsg)
        return msg
    }
}