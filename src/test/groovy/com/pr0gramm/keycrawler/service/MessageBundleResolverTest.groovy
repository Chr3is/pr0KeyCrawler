package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.model.MessageCodes
import spock.lang.Specification
import spock.lang.Unroll

class MessageBundleResolverTest extends Specification {

    @Unroll
    def 'message code=#code is found'(MessageCodes code) {
        expect:
        MessageBundleResolver.getMessage(code)

        where:
        code << MessageCodes.values()
    }

}
