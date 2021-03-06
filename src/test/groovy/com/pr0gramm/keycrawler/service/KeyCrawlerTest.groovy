package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.FileLoaderUtil
import com.pr0gramm.keycrawler.api.Content
import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.client.Pr0grammClient
import com.pr0gramm.keycrawler.client.Pr0grammImageClient
import com.pr0gramm.keycrawler.model.KeyResult
import com.pr0gramm.keycrawler.service.tesseract.TesseractService
import org.springframework.core.io.ByteArrayResource
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject

import java.nio.ByteBuffer

class KeyCrawlerTest extends Specification {

    Post postWithKey = new Post(id: 1, image: 'keyImage.png', user: 'awesomeDude', created: 100)

    ByteArrayResource image = new ByteArrayResource(FileLoaderUtil.getImageFile('steamKey.png').bytes)

    Pr0grammClient pr0grammApiClient = Mock()

    Pr0grammImageClient pr0grammImageClient = Mock()

    TesseractService tesseractService = Mock()

    ImagePreprocessingService imagePreprocessingService = Mock()

    @Subject
    KeyCrawler keyCrawler = new KeyCrawler(pr0grammApiClient, pr0grammImageClient, tesseractService, imagePreprocessingService)

    def 'init tries to login authenticated in and sets time of the latest post'() {
        when:
        keyCrawler.init().block()

        then:
        keyCrawler.dateTimeOfLastAnalyzedPost.get() == postWithKey.created

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.just(new Content(posts: [postWithKey]))
    }

    def 'keys can be crawled'() {
        when:
        List<KeyResult> result = keyCrawler.checkForNewKeys().block()

        then:
        !result.empty
        verifyAll(result[0]) {
            post == postWithKey
            keys.contains('8NONA-M7B7W-WB2JT')
        }

        keyCrawler.dateTimeOfLastAnalyzedPost.get() == postWithKey.created

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.just(new Content(posts: [postWithKey]))
        1 * pr0grammImageClient.getImage(postWithKey) >> Mono.just(Tuples.of(postWithKey, image))
        1 * imagePreprocessingService.process(Tuples.of(postWithKey, image)) >> Mono.just(Tuples.of(postWithKey, ByteBuffer.wrap(image.byteArray)))
        1 * tesseractService.extractTextFromImage(Tuples.of(postWithKey, ByteBuffer.wrap(image.byteArray))) >> Mono.just(Tuples.of(postWithKey, '8NONA-M7B7W-WB2JT'))
    }

    def 'posts which were already analyzed are skipped'() {
        given:
        keyCrawler.dateTimeOfLastAnalyzedPost.set(1000)

        when:
        List<KeyResult> result = keyCrawler.checkForNewKeys().block()

        then:
        result.empty

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.just(new Content(posts: [postWithKey]))
        0 * pr0grammImageClient.getImage(_)
        0 * tesseractService.extractTextFromImage(_)
    }

    def 'post which are not supported will be skipped'() {
        given:
        Post post = new Post(created: 10, image: 'test.mp4')

        when:
        List<KeyResult> result = keyCrawler.checkForNewKeys().block()

        then:
        result.empty

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.just(new Content(posts: [post]))
        0 * pr0grammImageClient.getImage(_)
        0 * tesseractService.extractTextFromImage(_)
    }

    def 'time will not be updated if there were no posts'() {
        when:
        keyCrawler.checkForNewKeys()

        then:
        keyCrawler.dateTimeOfLastAnalyzedPost.get() == 0

        and:
        1 * pr0grammApiClient.fetchNewContent() >> Mono.empty()
    }
}
