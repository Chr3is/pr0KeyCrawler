package com.pr0gramm.crawler.service


import com.pr0gramm.crawler.model.client.Pr0Post
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths

class ImagePreprocessingServiceTest extends Specification {

    static final Path EAST_DETECTION_FILE_PATH = Paths.get(new ClassPathResource("east/frozen_east_text_detection.pb").getURI())

    @Subject
    ImagePreprocessingService imagePreprocessingService = new ImagePreprocessingService(EAST_DETECTION_FILE_PATH)

    def 'image is preprocessed'() {
        given:
        ByteArrayResource image = new ByteArrayResource(new ClassPathResource("images/steamKey.png").getInputStream().readAllBytes())

        when:
        ByteBuffer buffer = imagePreprocessingService.process(Tuples.of(new Pr0Post(), image)).block().getT2()

        then:
        buffer.hasRemaining()
    }

    @Unroll
    def '#imageName contains text=#hasText'(String imageName, boolean hasText) {
        given:
        ByteArrayResource image = new ByteArrayResource(new ClassPathResource("images/$imageName").inputStream.readAllBytes())

        expect:
        imagePreprocessingService.process(Tuples.of(new Pr0Post(), image)).hasElement().block() == hasText

        where:
        imageName               || hasText
        'steamKey.png'          || true
        'landscapeWithText.jpg' || true
        'news.png'              || true
        'dog.jpg'               || false
    }

}
