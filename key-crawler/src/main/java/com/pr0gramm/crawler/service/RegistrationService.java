package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.api.model.NewPr0Message;
import com.pr0gramm.crawler.config.properties.RegistrationProperties;
import com.pr0gramm.crawler.model.Pr0User;
import com.pr0gramm.crawler.model.client.Pr0Message;
import com.pr0gramm.crawler.repository.User;
import com.pr0gramm.crawler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.UUID;

import static com.pr0gramm.crawler.model.MessageCodes.PR0GRAMM_MSG_REGISTRATION;
import static com.pr0gramm.crawler.service.MessageBundleResolver.getMessage;
import static com.pr0gramm.crawler.service.UserService.USER_TOKEN_DELIMITER;
import static com.pr0gramm.crawler.util.DatabaseUtils.handleDbRequest;

@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationProperties registrationProperties;

    private final UserRepository userRepository;

    private final Pr0grammMessageService pr0grammMessageService;

    public Mono<Void> handleNewRegistrations() {
        return pr0grammMessageService.getPendingMessages()
                .filter(messagesByUser -> containsRegistrationMessage(messagesByUser.getT2()))
                .flatMap(messagesByUser -> registerNewUser(messagesByUser.getT1()).map(user -> Tuples.of(messagesByUser.getT1(), user)))
                .flatMap(registeredUser -> Mono.zip(pr0grammMessageService.markMessagesAsReadFor(registeredUser.getT1()),
                        pr0grammMessageService.sendNewMessage(createMessage(registeredUser))))
                .then();
    }

    public Mono<User> registerNewUser(Pr0User user) {
        return handleDbRequest(userRepository.getByUserName(user.getName()))
                .hasElement()
                .flatMap(userExists -> {
                    if (userExists) {
                        log.info("User={} already exists", user);
                        return Mono.empty();
                    }
                    Mono<User> newRegisteredUser = handleDbRequest(userRepository.save(new User(user.getId(), user.getName(), generateToken())));
                    log.info("New user={} was registered", user);
                    return newRegisteredUser;
                });
    }

    private boolean containsRegistrationMessage(List<Pr0Message> messages) {
        return messages
                .stream()
                .anyMatch(message -> message.getMessage() != null &&
                        message.getMessage().toLowerCase().contains(registrationProperties.getKeyWord().toLowerCase()));
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private NewPr0Message createMessage(Tuple2<Pr0User, User> user) {
        Pr0User pr0User = user.getT1();
        String message = String.format(getMessage(PR0GRAMM_MSG_REGISTRATION), pr0User.getName() + USER_TOKEN_DELIMITER + user.getT2().getToken());
        return new NewPr0Message(pr0User, message);
    }
}
