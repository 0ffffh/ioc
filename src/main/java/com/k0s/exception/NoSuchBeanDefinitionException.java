package com.k0s.exception;

public class NoSuchBeanDefinitionException extends RuntimeException{

    public NoSuchBeanDefinitionException(String id, String clazzName, String instanceName) {
        super("No qualifying bean of type " + clazzName + " with id " + id + " is defined. Bean is of type " + instanceName);
    }
}
