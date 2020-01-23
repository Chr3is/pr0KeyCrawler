package com.pr0gramm.keycrawler.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class Pr0User {

    private final Long userId;

    private final String userName;

}
