package com.pr0gramm.crawler.service.telegram;

import com.google.common.util.concurrent.RateLimiter;
import com.pr0gramm.crawler.config.properties.TelegramProperties;
import com.pr0gramm.crawler.model.KeyResult;
import com.pr0gramm.crawler.model.Pr0User;
import com.pr0gramm.crawler.model.TelegramMessage;
import com.pr0gramm.crawler.service.RegistrationService;
import com.pr0gramm.crawler.service.UserService;
import com.pr0gramm.crawler.util.OptionalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static com.pr0gramm.crawler.service.MessageBundleResolver.getMessage;
import static org.telegram.abilitybots.api.objects.Flag.*;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
public class TelegramBot extends DefaultTelegramBot {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final UserService userService;

    private final Optional<RegistrationService> registrationService;

    private final RateLimiter rateLimiter;

    public TelegramBot(TelegramProperties telegramProperties, UserService userService, Optional<RegistrationService> registrationService) {
        super(telegramProperties, new DefaultBotOptions());
        this.userService = userService;
        this.registrationService = registrationService;
        this.rateLimiter = RateLimiter.create(30);
    }

    public Mono<Void> sendMessage(List<KeyResult> keyResults) {
        if (keyResults.isEmpty()) {
            return Mono.empty();
        }

        TelegramMessage telegramMessage = createTelegramMessage(keyResults);
        return sendMessageToAllUsers(telegramMessage);
    }

    private Mono<Void> sendMessageToAllUsers(TelegramMessage telegramMessage) {
        return userService.getAllVerifiedAndSubscribedUsers()
                .flatMap(user -> sendMessage(user.getChatId(), telegramMessage))
                .then();
    }

    private Mono<Void> sendMessage(long chatId, TelegramMessage telegramMessage) {
        return Mono.fromSupplier(() -> {
            rateLimiter.acquire();
            silent.send(telegramMessage.getMessage(), chatId);
            return null;
        });
    }

    public Ability subscribe() {
        return Ability
                .builder()
                .name("subscribe")
                .info(getMessage(MessageCodes.TELEGRAM_BOT_SUBSCRIBE_DESC))
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(this::subscribe)
                .build();
    }

    private void subscribe(MessageContext messageContext) {
        long chatId = messageContext.chatId();
        userService.subscribeUser(chatId)
                .doOnNext(user -> silent.send(getMessage(MessageCodes.TELEGRAM_BOT_SUBSCRIBE_MSG_SUCCESSFUL), chatId))
                .doOnError(throwable -> silent.send(getMessage(MessageCodes.TELEGRAM_BOT_SUBSCRIBE_MSG_UNSUCCESSFUL), chatId))
                .subscribe();
    }

    public Ability unsubscribe() {
        return Ability
                .builder()
                .name("unsubscribe")
                .info(getMessage(MessageCodes.TELEGRAM_BOT_UNSUBSCRIBE_DESC))
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(this::unsubscribe)
                .build();
    }

    private void unsubscribe(MessageContext ctx) {
        long chatId = ctx.chatId();
        userService.unsubscribeUser(chatId)
                .doOnNext(user -> silent.send(getMessage(MessageCodes.TELEGRAM_BOT_UNSUBSCRIBE_MSG_SUCCESSFUL), chatId))
                .doOnError(throwable -> silent.send(getMessage(MessageCodes.TELEGRAM_BOT_UNSUBSCRIBE_MSG_UNSUCCESSFUL), chatId))
                .subscribe();
    }

    public Ability deleteAccount() {
        return Ability
                .builder()
                .name("delete")
                .info(getMessage(MessageCodes.TELEGRAM_BOT_DELETE_DESC))
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(this::deleteAccount)
                .build();
    }

    private void deleteAccount(MessageContext ctx) {
        userService.deleteUser(ctx.chatId())
                .doOnNext(deleted -> silent.send(getMessage(MessageCodes.TELEGRAM_BOT_DELETE_MSG_SUCCESSFUL), ctx.chatId()))
                .doOnError(throwable -> silent.send(getMessage(MessageCodes.TELEGRAM_BOT_DELETE_MSG_UNSUCCESSFUL), ctx.chatId()))
                .subscribe();
    }

    public Ability addUserWithoutPr0gramm() {
        return Ability
                .builder()
                .name("add")
                .info(getMessage(MessageCodes.TELEGRAM_BOT_ADD_DESC))
                .input(1)
                .locality(USER)
                .privacy(CREATOR)
                .action(this::addUserWithoutPr0gramm)
                .build();
    }

    private void addUserWithoutPr0gramm(MessageContext ctx) {
        String username = ctx.firstArg();
        long chatId = ctx.chatId();
        OptionalUtils.execute(registrationService.map(service -> service.registerNewUser(new Pr0User(null, username))))
                .doOnNext(user -> silent.send(String.format(getMessage(MessageCodes.TELEGRAM_BOT_ADD_MSG_SUCCESSFUL), user.getToken()), chatId))
                .doOnError(throwable -> silent.send(String.format(getMessage(MessageCodes.TELEGRAM_BOT_ADD_MSG_UNSUCCESSFUL), username), chatId))
                .subscribe();
    }

    public Ability sendNotificationToAllUsers() {
        return Ability
                .builder()
                .name("notify")
                .info(getMessage(MessageCodes.TELEGRAM_BOT_NOTIFY))
                .input(0)
                .locality(USER)
                .privacy(CREATOR)
                .action(this::sendNotificationToAllUsers)
                .build();
    }

    private void sendNotificationToAllUsers(MessageContext ctx) {
        String notification = (ctx.arguments() == null) ? "" : String.join(" ", ctx.arguments());
        if (!StringUtils.isEmpty(notification)) {
            String msg = String.format(getMessage(MessageCodes.TELEGRAM_BOT_NOTIFY_MSG), notification);
            TelegramMessage telegramMessage = new TelegramMessage(msg);
            sendMessageToAllUsers(telegramMessage).subscribe();
        }
    }

    public Ability authenticateAccount() {
        return Ability
                .builder()
                .name("authenticate")
                .info(getMessage(MessageCodes.TELEGRAM_BOT_REGISTRATION_DESC))
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.forceReply(getMessage(MessageCodes.TELEGRAM_MSG_REPLY_TO_REGISTRATION), ctx.chatId()))
                .reply(this::authenticateAccount, MESSAGE, TEXT, REPLY)
                .build();
    }

    private void authenticateAccount(Update update) {
        Mono.fromSupplier(() -> isReply(update))
                .flatMap(isReply -> {
                    if (isReply) {
                        if (hasCorrectSyntax(update)) {
                            return authenticate(update);
                        } else {
                            silent.forceReply(getMessage(MessageCodes.TELEGRAM_MSG_REPLY_TO_REGISTRATION_SYNTAX_ERROR), update.getMessage().getChatId());
                        }
                    }
                    return Mono.empty();
                }).subscribe();
    }

    private boolean isReply(Update update) {
        String replyToMsg = update.getMessage().getReplyToMessage().getText();
        return replyToMsg.equals(getMessage(MessageCodes.TELEGRAM_MSG_REPLY_TO_REGISTRATION))
                || replyToMsg.equals(getMessage(MessageCodes.TELEGRAM_MSG_REPLY_TO_REGISTRATION_SYNTAX_ERROR))
                || replyToMsg.equals(getMessage(MessageCodes.TELEGRAM_MSG_REPLY_TO_REGISTRATION_AUTHENTICATION_FAILED));
    }

    private boolean hasCorrectSyntax(Update update) {
        return getUserNameAndToken(update).length == 2;
    }

    private Mono<Void> authenticate(Update update) {
        String[] userNameAndToken = getUserNameAndToken(update);
        String userName = userNameAndToken[0];
        String token = userNameAndToken[1];
        return userService.authenticateUser(update.getMessage().getChatId(), userName, token)
                .doOnNext(user -> silent.send(getMessage(MessageCodes.TELEGRAM_MSG_SUCCESSFUL_AUTHENTICATION), update.getMessage().getChatId()))
                .doOnError(throwable -> silent.forceReply(getMessage(MessageCodes.TELEGRAM_MSG_REPLY_TO_REGISTRATION_AUTHENTICATION_FAILED), update.getMessage().getChatId()))
                .then();
    }

    private String[] getUserNameAndToken(Update update) {
        String message = update.getMessage().getText();
        return (message == null) ? EMPTY_STRING_ARRAY : message.split(UserService.USER_TOKEN_DELIMITER);
    }

    private TelegramMessage createTelegramMessage(List<KeyResult> keyResults) {
        StringBuilder builder = new StringBuilder();
        keyResults.forEach(keyResult -> {
            builder.append(String.format(getMessage(MessageCodes.TELEGRAM_MSG_NEW_KEYS), keyResult.getPost().getUser(), keyResult.getPost().getFullUrl()));
            builder.append(keyResult.getKeysFormatted());
        });
        return new TelegramMessage(builder.toString());
    }
}
