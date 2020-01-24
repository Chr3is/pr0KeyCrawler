package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.api.Message;
import com.pr0gramm.keycrawler.config.properties.RegistrationProperties;
import com.pr0gramm.keycrawler.model.Pr0User;
import com.pr0gramm.keycrawler.repository.User;
import com.pr0gramm.keycrawler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.pr0gramm.keycrawler.util.DatabaseUtils.handleDbRequest;

@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationProperties registrationProperties;

    private final UserRepository userRepository;

    private final Pr0grammMessageService pr0grammMessageService;

    public Mono<Void> handleNewRegistrations() {
        return pr0grammMessageService.getPendingMessages()
                .filter(messagesByUser -> containsRegistrationMessage(messagesByUser.getT2()))
                .flatMap(messagesByUser -> registerNewUser(messagesByUser.getT1()))
                .flatMap(registeredUser -> Mono.zip(pr0grammMessageService.markMessagesAsReadFor(registeredUser),
                        pr0grammMessageService.sendNewMessage(registeredUser)))
                .then();
    }

    public Mono<User> registerNewUser(Pr0User user) {
        return handleDbRequest(userRepository.getByUserName(user.getUserName()))
                .hasElement()
                .flatMap(userExists -> {
                    if (userExists) {
                        log.info("User={} already exists", user);
                        return Mono.empty();
                    }
                    Mono<User> newRegisteredUser = handleDbRequest(userRepository.save(new User(user.getUserId(), user.getUserName(), generateToken())));
                    log.info("New user={} was registered", user);
                    return newRegisteredUser;
                });
    }

    private boolean containsRegistrationMessage(List<Message> messages) {
        return messages
                .stream()
                .anyMatch(message -> message.getMessage() != null &&
                        message.getMessage().toLowerCase().contains(registrationProperties.getKeyWord().toLowerCase()));
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
