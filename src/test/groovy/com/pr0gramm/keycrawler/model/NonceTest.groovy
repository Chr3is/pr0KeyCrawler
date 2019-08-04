package com.pr0gramm.keycrawler.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.pr0gramm.keycrawler.config.properties.Pr0grammApiClientProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [JacksonAutoConfiguration])
class NonceTest extends Specification {

    @Autowired
    ObjectMapper objectMapper

    def 'nonce can be created from me cookie'() {
        given:
        Pr0grammApiClientProperties properties = new Pr0grammApiClientProperties(cookies: ['Me': '%7B%22n%22%3A%22Test%22%2C%22id%22%3A%22123456789abcdefghijk%22%7D'])

        when:
        Nonce nonce = new Nonce(properties, objectMapper)

        then:
        nonce.value == '123456789abcdefg'
    }
}
