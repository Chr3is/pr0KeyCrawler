package com.pr0gramm.keycrawler.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

@UtilityClass
@Slf4j
public class ExternalFileLoaderUtil {

    private static final String EAST_TEXT_DETECTION_FILE = "target/resources-external/east/frozen_east_text_detection.pb";
    private static final String TESS_DATA_LOCATION = "target/resources-external/tessdata/";

    public static String getEastFilePath() {
        return Paths.get(EAST_TEXT_DETECTION_FILE).toAbsolutePath().toString();
    }

    public static String getTessDataPath() {
        return Paths.get(TESS_DATA_LOCATION).toAbsolutePath().toString();
    }

}
