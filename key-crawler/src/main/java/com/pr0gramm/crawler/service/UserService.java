package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.config.properties.RegistrationProperties;
import com.pr0gramm.crawler.exception.AuthenticationFailedException;
import com.pr0gramm.crawler.exception.CouldNotFindUserException;
import com.pr0gramm.crawler.repository.User;
import com.pr0gramm.crawler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.pr0gramm.crawler.util.DatabaseUtils.handleDbRequest;

@RequiredArgsConstructor
@Service
@EnableConfigurationProperties(RegistrationProperties.class)
@Slf4j
public class UserService {

    public final static String USER_TOKEN_DELIMITER = ":";

    private final UserRepository userRepository;

    public Flux<User> getAllVerifiedAndSubscribedUsers() {
        return handleDbRequest(userRepository.getAllByStatusAndSubscribed(true, true));
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

}
