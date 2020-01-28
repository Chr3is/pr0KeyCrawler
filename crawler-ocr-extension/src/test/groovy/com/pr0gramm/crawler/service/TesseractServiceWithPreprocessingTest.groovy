package com.pr0gramm.crawler.service


import com.pr0gramm.crawler.config.properties.ExternalFilesProperties
import com.pr0gramm.crawler.model.client.Pr0Post
import com.pr0gramm.crawler.service.tesseract.TesseractPool
import com.pr0gramm.crawler.service.tesseract.TesseractService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths

class TesseractServiceWithPreprocessingTest extends Specification {

    static final Path EAST_DETECTION_FILE_PATH = Paths.get(new ClassPathResource("east/frozen_east_text_detection.pb").getURI())

    static final Path TESS_DATA_PATH = Paths.get(new ClassPathResource("tessdata").getURI())

    ByteArrayResource image = new ByteArrayResource(new ClassPathResource("images/steamKey.png").getInputStream().readAllBytes())

    ExternalFilesProperties externalFilesProperties = new ExternalFilesProperties()

    ImagePreprocessingService imagePreprocessingService = new ImagePreprocessingService(EAST_DETECTION_FILE_PATH)

    TesseractPool pool = new TesseractPool(TESS_DATA_PATH)

    TesseractService tesseractService = new TesseractService(pool)

    def 'key can be extracted'() {
        given:
        Pr0Post post = new Pr0Post(id: 12345, contentLink: 'steamKey.png')
        Tuple2<Pr0Post, ByteBuffer> processedImage = imagePreprocessingService.process(Tuples.of(post, image)).block()

        when:
        Tuple2<Pr0Post, String> result = tesseractService.extractTextFromImage(processedImage).block()

        then:
        verifyAll(result) {
            verifyAll(result.t1) {
                id == post.id
                contentLink == post.contentLink
            }
            verifyAll(result.t2) {
                it.contains('8NONA-M7B7W-WB2JT')
            }
        }
    }

}
