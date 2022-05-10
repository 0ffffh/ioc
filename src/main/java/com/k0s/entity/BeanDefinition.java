package com.k0s.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class BeanDefinition {
    private String id;
    private String className;
    private Map<String, String> valueDependencies;
    private Map<String, String> refDependencies;
    private String initMethod;


    public BeanDefinition(String id, String className) {
        this.id = id;
        this.className = className;
    }

}