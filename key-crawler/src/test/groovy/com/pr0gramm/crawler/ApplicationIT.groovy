package com.pr0gramm.crawler

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class ApplicationIT extends Specification {

    def 'application starts'() {
        expect:
        assert true
    }
}