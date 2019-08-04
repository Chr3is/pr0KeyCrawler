package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.model.Pr0User;
import com.pr0gramm.keycrawler.repository.User;
import com.pr0gramm.keycrawler.repository.UserRepository;
import com.pr0gramm.keycrawler.service.exception.AuthenticationFailedException;
import com.pr0gramm.keycrawler.service.exception.CouldNotFindUserException;
import com.pr0gramm.keycrawler.service.exception.DatabaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    public final static String USER_TOKEN_DELIMITER = ":";

    private final UserRepository userRepository;

    private final Pr0grammMessageService pr0GrammMessageService;

    public Flux<User> getAllVerifiedAndSubscribedUsers() {
        return handleDbRequest(userRepository.getAllByStatusAndSubscribed(true, true));
    }

    public Flux<Void> handleNewRegistrations() {
        return pr0GrammMessageService.getUsersWithPendingMessages()
                .flatMap(this::registerNewUser)
                .flatMap(pr0GrammMessageService::sendNewMessage);
    }

    public Mono<User> registerNewUser(Pr0User user) {
        return handleDbRequest(userRepository.getByUserName(user.getUserName()))
                .hasElement()
                .flatMap(userExists -> {
                    if (userExists) {
                        log.error("User with username={} already exists", user.getUserName());
                        return Mono.empty();
                    }
                    Mono<User> newRegisteredUser = handleDbRequest(userRepository.save(new User(user.getUserId(), user.getUserName(), generateToken())));
                    log.info("New user {} was registered", user.getUserName());
                    return newRegisteredUser;
                });
    }

    public Mono<User> authenticateUser(long chatId, String userName, String token) {
        log.info("Authenticating user={}", userName);
        return handleDbRequest(userRepository.getByUserName(userName))
                .flatMap(user -> {
                    if (user.getToken().equals(token)) {
                        user.setVerified(true);
                        user.setChatId(chatId);
                        Mono<User> authenticatedUser = handleDbRequest(userRepository.save(user));
                        log.info("Authenticated user {} successfully", user.getUserName());
                        return authenticatedUser;
                    }
                    log.error("Could not authenticate user {}", user.getUserName());
                    return Mono.error(new AuthenticationFailedException(String.format("Could not authenticate user=%s", user)));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Could not find user={}", userName);
                    return Mono.error(new CouldNotFindUserException(String.format("Could not find user=%s", userName)));
                }));
    }

    public Mono<User> subscribeUser(long chatId) {
        return handleDbRequest(userRepository.getByChatId(chatId))
                .flatMap(user -> {
                    user.setSubscribed(true);
                    Mono<User> subscribedUser = handleDbRequest(userRepository.save(user));
                    log.info("User {} subscribed", user.getUserName());
                    return subscribedUser;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Could not find user with chatId={}", chatId);
                    return Mono.error(new CouldNotFindUserException(String.format("Could not find user by chatId=%s", chatId)));
                }));
    }

    public Mono<User> unsubscribeUser(long chatId) {
        return handleDbRequest(userRepository.getByChatId(chatId))
                .flatMap(user -> {
                    user.setSubscribed(false);
                    Mono<User> unsubscribedUser = handleDbRequest(userRepository.save(user));
                    log.info("User {} unsubscribed", user.getUserName());
                    return unsubscribedUser;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Could not find user with chatId={}", chatId);
                    return Mono.error(new CouldNotFindUserException(String.format("Could not find user by chatId=%s", chatId)));
                }));
    }

    public Mono<Boolean> deleteUser(long chatId) {
        Mono<Boolean> deletedUser = handleDbRequest(userRepository.deleteByChatId(chatId)).map(user -> true);
        log.info("User with chatId {} was deleted", chatId);
        return deletedUser;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private <T> Flux<T> handleDbRequest(Flux<T> flux) {
        return flux.onErrorResume(throwable -> {
            log.error("Error while executing database request", throwable);
            return Mono.error(new DatabaseException("Error while executing database request", throwable));
        });
    }

    private <T> Mono<T> handleDbRequest(Mono<T> mono) {
        return mono.onErrorResume(throwable -> {
            log.error("Error while executing database request", throwable);
            return Mono.error(new DatabaseException("Error while executing database request", throwable));
        });
    }
}
