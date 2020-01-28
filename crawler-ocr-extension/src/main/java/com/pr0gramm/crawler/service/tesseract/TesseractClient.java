package com.pr0gramm.crawler.service.tesseract;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.BytePointer;
import stormpot.Poolable;
import stormpot.Slot;

import java.nio.file.Path;

import static org.bytedeco.javacpp.lept.PIX;
import static org.bytedeco.javacpp.tesseract.TessBaseAPI;

@Slf4j
public class TesseractClient implements Poolable {

    private static final String LANGUAGE = "eng";

    private final TessBaseAPI tessBaseAPI;

    private final Slot slot;

    public TesseractClient(Path tessDataLocation, Slot slot) {
        this.slot = slot;
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.Init(tessDataLocation.toString(), LANGUAGE, 1);
        this.tessBaseAPI = tessBaseAPI;
        log.info("Created new TesseractClient Instance");
    }

    public String getTextFrom(PIX image) {
        tessBaseAPI.SetImage(image);
        BytePointer pointer = tessBaseAPI.GetUTF8Text();
        return pointer == null ? "" : pointer.getString();
    }

    public void deallocate() {
        tessBaseAPI.End();
    }

    @Override
    public void release() {
        tessBaseAPI.Clear();
        tessBaseAPI.ClearAdaptiveClassifier();
        slot.release(this);
    }

}
