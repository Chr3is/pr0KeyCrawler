package com.pr0gramm.crawler.service.exception;

public class CouldNotAcquireTesseractClientException extends RuntimeException {

    public CouldNotAcquireTesseractClientException(Throwable e) {
        super(e);
    }

    public CouldNotAcquireTesseractClientException(String message) {
        super(message);
    }
}
