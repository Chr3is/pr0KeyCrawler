package com.pr0gramm.crawler.service

import com.pr0gramm.crawler.FileLoaderUtil
import com.pr0gramm.crawler.config.properties.ExternalFilesProperties
import com.pr0gramm.crawler.model.client.Pr0Post
import org.springframework.core.io.ByteArrayResource
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.ByteBuffer

class ImagePreprocessingServiceTest extends Specification {

    ExternalFilesProperties externalFilesProperties = new ExternalFilesProperties()

    @Subject
    ImagePreprocessingService imagePreprocessingService = new ImagePreprocessingService(externalFilesProperties)

    def 'image is preprocessed'() {
        given:
        File file = FileLoaderUtil.getImageFile('steamKey.png')

        when:
        ByteBuffer buffer = imagePreprocessingService.process(Tuples.of(new Pr0Post(), new ByteArrayResource(file.bytes))).block().getT2()

        then:
        buffer.hasRemaining()
    }

    @Unroll
    def '#imageName contains text=#hasText'(String imageName, boolean hasText) {
        given:
        File image = FileLoaderUtil.getImageFile(imageName)

        expect:
        imagePreprocessingService.process(Tuples.of(new Pr0Post(), new ByteArrayResource(image.bytes))).hasElement().block() == hasText

        where:
        imageName               || hasText
        'steamKey.png'          || true
        'landscapeWithText.jpg' || true
        'news.png'              || true
        'dog.jpg'               || false
    }

}