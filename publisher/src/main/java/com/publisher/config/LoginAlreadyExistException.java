package com.publisher.config;

public class LoginAlreadyExistException extends RuntimeException {

    public LoginAlreadyExistException(String s) {
        super(s);
    }
}
