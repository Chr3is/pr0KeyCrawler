package com.pr0gramm.crawler.handler.image;

import com.pr0gramm.crawler.api.listeners.images.ImageTextListener;
import com.pr0gramm.crawler.handler.Handler;
import com.pr0gramm.crawler.model.client.Pr0Post;
import com.pr0gramm.crawler.service.ImagePreprocessingService;
import com.pr0gramm.crawler.service.tesseract.TesseractService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageTextHandler implements Handler<List<Tuple2<Pr0Post, ByteArrayResource>>> {

    private final TesseractService tesseractService;

    private final ImagePreprocessingService imagePreprocessingService;

    private final List<ImageTextListener> imageTextListeners;

    @Override
    public Mono<Void> process(List<Tuple2<Pr0Post, ByteArrayResource>> input) {
        return Flux.fromIterable(input)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(imagePreprocessingService::process)
                .flatMap(tesseractService::extractTextFromImage)
                .sequential()
                .collectList()
                .doOnNext(imageTextByPost -> imageTextListeners.forEach(listener -> listener.processImageText(imageTextByPost)))
                .then();
    }
}
