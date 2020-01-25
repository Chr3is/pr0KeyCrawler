package com.pr0gramm.keycrawler.api;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("image")
    private String contentLink;

    private String user;

    public boolean isAfter(long lastTimeAnalyzed) {
        return created > lastTimeAnalyzed;
    }

    public String getImageType() {
        return isSupported() ? contentLink.split("\\.")[1] : "";
    }

    public boolean isSupported() {
        return (contentLink != null) && SUPPORTED_FORMAT.matcher(contentLink).matches();
    }

    @Override
    public int compareTo(Post o) {
        return (int) (o.getCreated() - this.created);
    }

    public void setFullUrl(String url) {
        this.fullUrl = url + id;
    }
}
