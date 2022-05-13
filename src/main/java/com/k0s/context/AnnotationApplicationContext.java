package com.k0s.context;

import com.k0s.beanFactory.AnnotationBeanFactory;
import com.k0s.beanFactory.BeanFactory;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AnnotationApplicationContext extends Context {

    private final BeanFactory beanFactory = new AnnotationBeanFactory();


    public AnnotationApplicationContext() {
    }

    public AnnotationApplicationContext(String basePackage) {
        this.createBeans(basePackage);
    }

    public void createBeans(String basePackage) {
            beanFactory.createBeans(beanMap, basePackage);
    }

}
