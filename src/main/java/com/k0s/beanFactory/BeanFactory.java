package com.k0s.beanFactory;

import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;

import java.util.Map;

public interface BeanFactory {
    default void createBeans(Map<String, Bean> beanMap, String basePackage){}
    default void createBeans(Map<String, Bean> beanMap, Map<String, BeanDefinition> beanDefinitions){}
    default <T> T createBean(Class<T> clazz){
        return null;
    }
    default Bean createBean(BeanDefinition beanDefinition){
        return null;
    }


}
