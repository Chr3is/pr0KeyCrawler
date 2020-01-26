package com.pr0gramm.crawler.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.pr0gramm.crawler.config.Pr0grammApiClientConfig
import com.pr0gramm.crawler.config.properties.Pr0grammApiClientProperties
import com.pr0gramm.crawler.model.Nonce
import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

@SpringBootTest(classes = [Pr0grammApiClientProperties, WebClientAutoConfiguration, JacksonAutoConfiguration])
@TestPropertySource(properties = ["spring.config.location=classpath:application.yml"])
@Slf4j
abstract class BaseIT extends Specification {

    static final String ENV_DELIMITER = ":"

    static final String USER_1_ENV = 'pr0.crawler.user1'
    static final String USER_2_ENV = 'pr0.crawler.user2'

    static Long ID_USER_1
    static String NAME_USER_1
    static String COOKIE_USER_1

    static Long ID_USER_2
    static String NAME_USER_2
    static String COOKIE_USER_2

    static {
        String firstUser = System.getenv()[USER_1_ENV]
        String secondUser = System.getenv()[USER_2_ENV]

        if (!skipTest()) {
            log.info("Real integration test will be executed due to given environment variables")
            String[] argsUser1 = firstUser.split(ENV_DELIMITER)
            String[] argsUser2 = secondUser.split(ENV_DELIMITER)

            ID_USER_1 = Long.parseLong(argsUser1[0])
            COOKIE_USER_1 = argsUser1[1]

            ID_USER_2 = Long.parseLong(argsUser2[0])
            COOKIE_USER_2 = argsUser2[1]
            log.info("Using following users\n User1 --> id: {} -- cookie: {}\n User2 --> id: {} -- cookie: {}", ID_USER_1, COOKIE_USER_1, ID_USER_2, COOKIE_USER_2)
        } else {
            log.info("Real integration test is not executed due to missing environment variables")
        }
    }

    static boolean skipTest() {
        return StringUtils.isEmpty(System.getenv()[USER_1_ENV]) || StringUtils.isEmpty(System.getenv()[USER_2_ENV])
    }

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    WebClient.Builder webClientBuilder

    @Autowired
    Pr0grammApiClientProperties baseProperties

    Pr0grammClient pr0grammClientUser1

    Pr0grammClient pr0grammClientUser2

    def setup() {
        NAME_USER_1 = getUserNameFrom(COOKIE_USER_1)
        NAME_USER_2 = getUserNameFrom(COOKIE_USER_2)
        pr0grammClientUser1 = createPr0grammClientWith(COOKIE_USER_1)
        pr0grammClientUser2 = createPr0grammClientWith(COOKIE_USER_2)
    }

    def createPr0grammClientWith(String meCookie) {
        Pr0grammApiClientProperties properties = getClientPropertiesWith(['me': meCookie])
        WebClient webClient = new Pr0grammApiClientConfig(properties).createPr0grammApiClient(webClientBuilder)
        Nonce nonce = new Nonce(properties, objectMapper)
        return new Pr0grammClient(properties, webClient, nonce)
    }

    def getClientPropertiesWith(Map cookies) {
        Pr0grammApiClientProperties clientProperties = new Pr0grammApiClientProperties()
        InvokerHelper.setProperties(clientProperties, baseProperties.properties)
        clientProperties.setCookies(cookies)
        return clientProperties
    }

    def getUserNameFrom(String cookie) {
        String decodedCookie = URLDecoder.decode(cookie, "UTF-8")
        return Objects.requireNonNull(objectMapper.readValue(decodedCookie, NonceContent).name)
    }

    static class NonceContent {
        @JsonProperty("n")
        String name
    }

}
