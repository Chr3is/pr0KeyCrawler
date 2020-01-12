package com.pr0gramm.keycrawler.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Pr0grammComment {

    private final long postId;

    private final String message;

}
