package com.pr0gramm.crawler.api.model;

import com.pr0gramm.crawler.model.Pr0User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NewPr0Message {

    public final Pr0User recipient;

    public final String message;
}
