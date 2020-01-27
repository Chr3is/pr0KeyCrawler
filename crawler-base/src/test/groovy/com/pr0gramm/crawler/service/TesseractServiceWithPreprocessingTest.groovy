package com.pr0gramm.crawler.service

import com.pr0gramm.crawler.FileLoaderUtil
import com.pr0gramm.crawler.config.properties.ExternalFilesProperties
import com.pr0gramm.crawler.model.client.Pr0Post
import com.pr0gramm.crawler.service.tesseract.TesseractPool
import com.pr0gramm.crawler.service.tesseract.TesseractService
import org.springframework.core.io.ByteArrayResource
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import spock.lang.Specification

import java.nio.ByteBuffer

class TesseractServiceWithPreprocessingTest extends Specification {

    File testImage = FileLoaderUtil.getImageFile('steamKey.png')

    ExternalFilesProperties externalFilesProperties = new ExternalFilesProperties()

    ImagePreprocessingService imagePreprocessingService = new ImagePreprocessingService(externalFilesProperties)

    TesseractPool pool = new TesseractPool(externalFilesProperties)

    TesseractService tesseractService = new TesseractService(pool)

    def 'key can be extracted'() {
        given:
        Pr0Post post = new Pr0Post(id: 12345, contentLink: 'steamKey.png')
        ByteArrayResource arrayResource = new ByteArrayResource(testImage.bytes)
        Tuple2<Pr0Post, ByteBuffer> processedImage = imagePreprocessingService.process(Tuples.of(post, arrayResource)).block()

        when:
        Tuple2<Pr0Post, String> result = tesseractService.extractTextFromImage(processedImage).block()

        then:
        verifyAll(result) {
            verifyAll(result.t1) {
                id == post.id
                contentLink == post.contentLink
            }
            verifyAll(result.t2) {
                it.contains('8NONA-M7B7W-WB2JT')//TODO
            }
        }
    }

}
