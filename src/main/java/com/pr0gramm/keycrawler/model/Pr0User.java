package com.pr0gramm.keycrawler.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Pr0User {

    private final Long userId;

    private final String userName;

}
