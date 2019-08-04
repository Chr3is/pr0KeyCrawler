package com.pr0gramm.keycrawler.utils

import spock.lang.Specification

class ExternalFileLoaderUtilTest extends Specification {

    def 'east file can be found'() {
        when:
        String path = ExternalFileLoaderUtil.eastFilePath

        then:
        new File(path).exists()
    }

    def 'tessdata dir can be found'() {
        when:
        String path = ExternalFileLoaderUtil.tessDataPath

        then:
        new File(path).exists()
    }
}
