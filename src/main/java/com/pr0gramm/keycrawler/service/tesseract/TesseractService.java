package com.pr0gramm.keycrawler.service.tesseract;

import com.pr0gramm.keycrawler.api.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

import static org.bytedeco.javacpp.lept.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class TesseractService {

    private static final Pattern UNWANTED_CHARS = Pattern.compile("[^ -~^\\s]");

    private final TesseractPool tesseractPool;

    public Mono<Tuple2<Post, String>> extractTextFromImage(Tuple2<Post, ByteBuffer> postWithImage) {
        return Mono.fromSupplier(() -> postWithImage.mapT2(image -> {
            Instant start = Instant.now();

            PIX pixImage = pixReadMem(image, image.capacity());
            image.clear();

            TesseractClient tesseractClient = tesseractPool.getInstance();
            String cleanResult = cleanString(tesseractClient.getTextFrom(pixImage));

            pixDestroy(pixImage);
            tesseractClient.release();

            log.debug("Extracted text={} from image with post={}", cleanResult, postWithImage.getT1());
            log.info("OCR took {} millis", Duration.between(start, Instant.now()).toMillis());
            return cleanResult;
        }));
    }

    private String cleanString(String output) {
        return UNWANTED_CHARS.matcher(new String(output.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
                .replaceAll("");
    }
}
