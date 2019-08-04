package com.pr0gramm.keycrawler.service.tesseract;

import lombok.extern.slf4j.Slf4j;
import stormpot.Allocator;
import stormpot.Slot;

@Slf4j
public class TesseractClientAllocator implements Allocator<TesseractClient> {

    @Override
    public TesseractClient allocate(Slot slot) {
        return new TesseractClient(slot);
    }

    @Override
    public void deallocate(TesseractClient client) {
        client.deallocate();
    }

}
