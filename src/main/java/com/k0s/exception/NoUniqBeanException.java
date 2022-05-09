package com.k0s.exception;

public class NoUniqBeanException extends RuntimeException{
    public NoUniqBeanException(String clazzName) {
        super(clazzName + " has 0 or more then 1 instance");
    }
}
