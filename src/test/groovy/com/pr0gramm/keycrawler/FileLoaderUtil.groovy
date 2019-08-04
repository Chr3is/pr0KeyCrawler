package com.pr0gramm.keycrawler

import lombok.experimental.UtilityClass

@UtilityClass
class FileLoaderUtil {

    static File getImageFile(String fileName) {
        return getFile("images/$fileName")
    }

    static File getFile(String fileName) {
        return new File(this.classLoader.getResource(fileName).file)
    }

}
