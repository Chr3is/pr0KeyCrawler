package com.pr0gramm.crawler.model;

import java.util.regex.Pattern;

public enum PostType {

    IMAGE,
    GIF,
    VIDEO;

    private static final Pattern IMAGE_TYPE_PATTERN = Pattern.compile("^.*\\.(jpeg|jpg|jpe|png)$");

    private static final Pattern GIF_TYPE_PATTERN = Pattern.compile("^.*\\.gif$");

    private static final Pattern VIDEO_TYPE_PATTERN = Pattern.compile("^.*\\.mp4$");

    public static PostType getFrom(String type) {
        if (IMAGE_TYPE_PATTERN.matcher(type).matches()) {
            return IMAGE;
        }

        if (VIDEO_TYPE_PATTERN.matcher(type).matches()) {
            return VIDEO;
        }

        if (GIF_TYPE_PATTERN.matcher(type).matches()) {
            return GIF;
        }
        throw new IllegalArgumentException(String.format("No PostType for post=%s", type));
    }
}
