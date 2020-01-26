package com.pr0gramm.crawler.api.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Pr0Message {

    public final Pr0User recipient;

    public final String message;
}
