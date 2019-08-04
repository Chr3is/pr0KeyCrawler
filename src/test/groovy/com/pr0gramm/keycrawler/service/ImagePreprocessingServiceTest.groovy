package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.FileLoaderUtil
import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.service.ImagePreprocessingService
import org.springframework.core.io.ByteArrayResource
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.ByteBuffer

class ImagePreprocessingServiceTest extends Specification {

    @Subject
    ImagePreprocessingService imagePreprocessingService = new ImagePreprocessingService()

    def 'image is preprocessed'() {
        given:
        File file = FileLoaderUtil.getImageFile('steamKey.png')

        when:
        ByteBuffer buffer = imagePreprocessingService.process(Tuples.of(new Post(), new ByteArrayResource(file.bytes))).block().getT2()

        then:
        buffer.hasRemaining()
    }

    @Unroll
    def '#imageName contains text=#hasText'(String imageName, boolean hasText) {
        given:
        File image = FileLoaderUtil.getImageFile(imageName)

        expect:
        imagePreprocessingService.process(Tuples.of(new Post(), new ByteArrayResource(image.bytes))).hasElement().block() == hasText

        where:
        imageName                  || hasText
        'steamKey.png'             || true
        'landscapeWithText.jpg'    || true
        'news.png'                 || true
        'dog.jpg'                  || false
        'landscapeWithoutText.jpg' || false
    }

}
