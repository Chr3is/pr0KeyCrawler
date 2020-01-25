package com.pr0gramm.keycrawler.model;

import com.pr0gramm.keycrawler.api.Post;

import java.util.regex.Pattern;

public enum PostType {

    IMAGE,
    GIF,
    VIDEO;

    private static final Pattern IMAGE_TYPE_PATTERN = Pattern.compile("^.*\\.(jpeg|jpg|jpe|png)$");

    private static final Pattern GIF_TYPE_PATTERN = Pattern.compile("^.*\\.gif$");

    private static final Pattern VIDEO_TYPE_PATTERN = Pattern.compile("^.*\\.mp4$");

    public static PostType getFrom(Post post) {
        if (post.getContentLink() != null) {
            if (IMAGE_TYPE_PATTERN.matcher(post.getContentLink()).matches()) {
                return IMAGE;
            }

            if (VIDEO_TYPE_PATTERN.matcher(post.getContentLink()).matches()) {
                return VIDEO;
            }

            if (GIF_TYPE_PATTERN.matcher(post.getContentLink()).matches()) {
                return GIF;
            }
        }
        throw new IllegalArgumentException(String.format("No PostType for post=%s", post));
    }
}
