package com.k0s.context;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class AnnotationApplicationContext extends Context {

    public AnnotationApplicationContext() {
    }

    public AnnotationApplicationContext(String basePackage) {
        this.createBeans(basePackage);
    }

    public void createBeans(String basePackage) {
        log.info("Scan {}, create beans for all @Component classes", basePackage);
        Optional<BeanFactory> beanFactoryOptional = Optional.ofNullable(this.beanFactory);
        if (beanFactoryOptional.isPresent()) {
            beanFactory.createBeans(beanMap, basePackage);
        } else {
            beanFactory = new BeanFactory();
            beanFactory.createBeans(beanMap, basePackage);
        }

    }


}
