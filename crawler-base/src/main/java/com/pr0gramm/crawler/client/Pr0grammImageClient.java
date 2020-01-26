package com.pr0gramm.crawler.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class Pr0grammImageClient extends Pr0grammContentClient {

    public Pr0grammImageClient(WebClient pr0ImageClient) {
        super(pr0ImageClient);
    }
}
