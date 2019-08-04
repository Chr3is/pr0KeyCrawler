package com.pr0gramm.keycrawler.service.tesseract;

import com.pr0gramm.keycrawler.service.exception.CouldNotAcquireTesseractClientException;
import org.bytedeco.javacpp.tesseract;
import org.springframework.stereotype.Service;
import stormpot.BlazePool;
import stormpot.Config;
import stormpot.Pool;
import stormpot.Timeout;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Service
public class TesseractPool {

    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final static Timeout TIMEOUT = new Timeout(5, TimeUnit.SECONDS);

    private final Pool<TesseractClient> tesseractClientPool;

    public TesseractPool() {
        TesseractClientAllocator tesseractClientAllocator = new TesseractClientAllocator();
        Config<TesseractClient> config = new Config<>().setAllocator(tesseractClientAllocator);
        config.setSize(POOL_SIZE);
        config.setExpiration(info -> false);
        this.tesseractClientPool = new BlazePool<>(config);
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
