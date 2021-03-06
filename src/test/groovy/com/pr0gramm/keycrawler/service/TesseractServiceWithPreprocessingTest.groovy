package com.pr0gramm.keycrawler.service

import com.pr0gramm.keycrawler.FileLoaderUtil
import com.pr0gramm.keycrawler.api.Post
import com.pr0gramm.keycrawler.config.properties.ExternalFilesProperties
import com.pr0gramm.keycrawler.model.KeyResult
import com.pr0gramm.keycrawler.service.tesseract.TesseractPool
import com.pr0gramm.keycrawler.service.tesseract.TesseractService
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
        Post post = new Post(id: 12345, image: 'steamKey.png')
        ByteArrayResource arrayResource = new ByteArrayResource(testImage.bytes)
        Tuple2<Post, ByteBuffer> processedImage = imagePreprocessingService.process(Tuples.of(post, arrayResource)).block()

        when:
        Tuple2<Post, String> result = tesseractService.extractTextFromImage(processedImage).block()
        KeyResult keyResult = new KeyResult(result)

        then:
        keyResult.post.id == 12345
        keyResult.keys.size() == 1
        keyResult.keys[0] == "8NONA-M7B7W-WB2JT"
    }

}
