package com.k0s.reader;

import com.k0s.entity.BeanDefinition;

import java.util.Map;

public interface BeanDefinitionReader {
    Map<String, BeanDefinition> getBeanDefinitionMap();
}