package com.pr0gramm.keycrawler.util

import com.pr0gramm.keycrawler.service.exception.DatabaseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class DatabaseUtilsTest extends Specification {

    def 'database exception is thrown if flux fails'() {
        given:
        Exception exception = new Exception('Something went wrong')

        when:
        DatabaseUtils.handleDbRequest(Flux.error(exception)).collectList().block()

        then:
        Throwable exp = thrown(DatabaseException)
        exp.message == 'Error while executing database request'
        exp.cause == exception
    }

    def 'database exception is thrown if mono fails'() {
        given:
        Exception exception = new Exception('Something went wrong')

        when:
        DatabaseUtils.handleDbRequest(Mono.error(exception)).block()

        then:
        Throwable exp = thrown(DatabaseException)
        exp.message == 'Error while executing database request'
        exp.cause == exception
    }
}
