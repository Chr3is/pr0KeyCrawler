package com.pr0gramm.crawler.api.model;

import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NewPr0Comment {

    private final Pr0Post post;

    private final String message;

}
