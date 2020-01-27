package com.pr0gramm.crawler.model.client;


import com.pr0gramm.crawler.model.PostType;
import lombok.Data;

@Data
public class Pr0Post implements Comparable<Pr0Post> {

    private long id;

    private String fullUrl;

    private long created = 0;

    private String contentLink;

    private String user;

    private PostType type;

    public boolean isAfter(long lastTimeAnalyzed) {
        return created > lastTimeAnalyzed;
    }

    public void setFullUrl(String url) {
        this.fullUrl = url + id;
    }

    @Override
    public int compareTo(Pr0Post o) {
        return (int) (o.getCreated() - this.created);
    }
}
