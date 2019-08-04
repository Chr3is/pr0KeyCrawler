package com.pr0gramm.keycrawler.api;

import lombok.Data;
import lombok.ToString;

import java.util.regex.Pattern;

@Data
@ToString
public class Post implements Comparable<Post> {

    private static final Pattern SUPPORTED_FORMAT = Pattern.compile("^.*\\.(jpeg|jpg|jpe|png)$");

    private long id;

    private String fullUrl;

    private long created = 0;

    private String image;

    private String user;

    public boolean isAfter(long lastTimeAnalyzed) {
        return created > lastTimeAnalyzed;
    }

    public String getImageType() {
        return isSupported() ? image.split("\\.")[1] : "";
    }

    public boolean isSupported() {
        return (image != null) && SUPPORTED_FORMAT.matcher(image).matches();
    }

    @Override
    public int compareTo(Post o) {
        return (int) (o.getCreated() - this.created);
    }

    public void setFullUrl(String url) {
        this.fullUrl = url + id;
    }
}
