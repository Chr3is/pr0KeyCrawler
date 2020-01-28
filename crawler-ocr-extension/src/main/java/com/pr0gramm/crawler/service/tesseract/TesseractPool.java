package com.pr0gramm.crawler.service.tesseract;

import com.pr0gramm.crawler.config.properties.ExternalFilesProperties;
import com.pr0gramm.crawler.service.exception.CouldNotAcquireTesseractClientException;
import org.bytedeco.javacpp.tesseract;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import stormpot.Pool;
import stormpot.Timeout;

import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(ExternalFilesProperties.class)
public class TesseractPool {

    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final static Timeout TIMEOUT = new Timeout(5, TimeUnit.SECONDS);

    private final Pool<TesseractClient> tesseractClientPool;

    public TesseractPool(Path tessDataLocation) {
        TesseractClientAllocator tesseractClientAllocator = new TesseractClientAllocator(tessDataLocation);
        this.tesseractClientPool = Pool.from(tesseractClientAllocator).setSize(POOL_SIZE).setExpiration(info -> false).build();
    }

    public TesseractClient getInstance() {
        TesseractClient client;

        try {
            client = tesseractClientPool.claim(TIMEOUT);
        } catch (InterruptedException e) {
            throw new CouldNotAcquireTesseractClientException(e);
        }

        if (client == null) {
            throw new CouldNotAcquireTesseractClientException("Could not get a tesseract client in the given time");
        }
        return client;
    }

    @PreDestroy
    public void destroy() {
        tesseract.TessBaseAPI.ClearPersistentCache();
    }

}
