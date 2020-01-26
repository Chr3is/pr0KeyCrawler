package com.pr0gramm.crawler.service.tesseract;

import com.pr0gramm.crawler.config.properties.ExternalFilesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stormpot.Allocator;
import stormpot.Slot;

@Slf4j
@RequiredArgsConstructor
public class TesseractClientAllocator implements Allocator<TesseractClient> {

    private final ExternalFilesProperties properties;

    @Override
    public TesseractClient allocate(Slot slot) {
        return new TesseractClient(properties, slot);
    }

    @Override
    public void deallocate(TesseractClient client) {
        client.deallocate();
    }

}
