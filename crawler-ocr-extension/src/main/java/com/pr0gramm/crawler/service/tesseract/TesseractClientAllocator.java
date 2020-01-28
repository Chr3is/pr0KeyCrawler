package com.pr0gramm.crawler.service.tesseract;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stormpot.Allocator;
import stormpot.Slot;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class TesseractClientAllocator implements Allocator<TesseractClient> {

    private final Path tessDataLocation;

    @Override
    public TesseractClient allocate(Slot slot) {
        return new TesseractClient(tessDataLocation, slot);
    }

    @Override
    public void deallocate(TesseractClient client) {
        client.deallocate();
    }

}
