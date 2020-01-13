package com.pr0gramm.keycrawler.client.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UnexpectedStatusCodeException extends RuntimeException {

    private final HttpStatus httpStatus;

    public UnexpectedStatusCodeException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
