package com.pr0gramm.keycrawler.service.exception;

public class CouldNotAcquireTesseractClientException extends RuntimeException {

    public CouldNotAcquireTesseractClientException(Throwable e) {
        super(e);
    }

    public CouldNotAcquireTesseractClientException(String message) {
        super(message);
    }
}
