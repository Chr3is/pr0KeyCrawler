package com.pr0gramm.keycrawler.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Pr0grammMessage {

    private final long userId;

    private final String userName;

    private final String message;

}
