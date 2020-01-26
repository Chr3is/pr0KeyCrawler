package com.pr0gramm.crawler.exception;

public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
