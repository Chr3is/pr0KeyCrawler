package com.pr0gramm.crawler.service

import com.pr0gramm.crawler.model.MessageCodes
import com.pr0gramm.crawler.service.MessageBundleResolver
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
