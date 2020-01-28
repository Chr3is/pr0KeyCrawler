package com.pr0gramm.crawler.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
@Slf4j
public class ClasspathFileExtractor {

    private static final Path TEMP_DIR_PATH;

    static {
        try {
            TEMP_DIR_PATH = Files.createTempDirectory("pr0crawler");
            TEMP_DIR_PATH.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temp directory to store the required external files", e);
        }
    }

    @SneakyThrows
    public static Path extractFile(String classPathFile, @Nullable FileSystemResource destDir) {
        InputStream fileStream = ClasspathFileExtractor.class.getClassLoader().getResourceAsStream(classPathFile);
        if (fileStream == null) {
            throw new RuntimeException(String.format("Could not load classpath resource '%s'", classPathFile));
        }
        Path filePath = getDestinationPath(classPathFile, destDir);
        if (filePath.toFile().exists()) {
            return filePath;
        }
        log.info("Copying classPathFile={} to {}", classPathFile, filePath.toString());
        createParents(filePath);
        Files.copy(fileStream, filePath);
        fileStream.close();
        return filePath;
    }

    @SneakyThrows
    private Path getDestinationPath(String classPathFile, @Nullable FileSystemResource destDir) {
        if (destDir == null) {
            return Paths.get(TEMP_DIR_PATH.toString(), classPathFile);
        } else {
            if (destDir.exists()) {
                return Paths.get(destDir.getURI());
            } else {
                return Paths.get(destDir.toString(), classPathFile);
            }
        }
    }

    @SneakyThrows
    private void createParents(Path path) {
        Path parent = path.getParent();
        if (!parent.toFile().exists()) {
            createParents(parent);
            Files.createDirectory(parent);
        }
    }
}