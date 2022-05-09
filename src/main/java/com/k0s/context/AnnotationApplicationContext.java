package com.k0s.context;

import java.util.*;


public class AnnotationApplicationContext extends Context {


    public AnnotationApplicationContext(String basePackage) {
        this.createBeans(basePackage);
    }

    public void createBeans(String basePackage){
        Optional<BeanFactory> beanFactoryOptional = Optional.ofNullable(this.beanFactory);
        if(beanFactoryOptional.isPresent()){
            beanFactory.createBeans(beanMap, basePackage);
        } else {
            beanFactory = new BeanFactory();
            beanFactory.createBeans(beanMap, basePackage);
        }

    }

}
