package com.k0s.exception;

public class PostConstructException extends RuntimeException {
    public PostConstructException(String name, Exception e) {
        super("Invoke method " + name +" error ", e);
    }
}
