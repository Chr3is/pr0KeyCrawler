package com.pr0gramm.crawler.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    @Query("SELECT * FROM account WHERE user_name = $1")
    Mono<User> getByUserName(String userName);

    @Query("SELECT * FROM account WHERE chat_id = $1")
    Mono<User> getByChatId(long chatId);

    @Query("SELECT * FROM account WHERE verified = $1 AND subscribed = $2")
    Flux<User> getAllByStatusAndSubscribed(boolean verified, boolean subscribed);

    @Query("DELETE FROM account WHERE chat_id = $1")
    Mono<User> deleteByChatId(long chatId);

}
