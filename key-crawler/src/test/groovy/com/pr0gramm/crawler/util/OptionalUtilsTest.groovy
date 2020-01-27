package com.pr0gramm.crawler.util

import reactor.core.publisher.Mono
import spock.lang.Specification

class OptionalUtilsTest extends Specification {

    def 'empty mono is returned if optional is not present'() {
        when:
        def result = OptionalUtils.execute(Optional.empty()).block()

        then:
        result == null
    }

    def 'result is returned if optional is present'() {
        given:
        String value = 'Hello World'

        expect:
        OptionalUtils.execute(Optional.of(Mono.just(value))).block() == value
    }
}
