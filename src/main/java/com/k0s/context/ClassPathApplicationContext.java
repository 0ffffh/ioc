package com.k0s.context;


import com.k0s.beanFactory.BeanFactory;
import com.k0s.beanFactory.ClassPathBeanFactory;
import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.reader.BeanDefinitionReader;
import com.k0s.reader.stax.XMLBeanDefinitionReader;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class ClassPathApplicationContext extends Context {
    private final BeanFactory beanFactory = new ClassPathBeanFactory();


    public ClassPathApplicationContext() {
    }

    public ClassPathApplicationContext(String path) {
        log.info("Scan {}, create beans", path);
        BeanDefinitionReader beanDefinitionReader = new XMLBeanDefinitionReader(path);
        this.createBeans(beanDefinitionReader.getBeanDefinitionMap());
    }

    public ClassPathApplicationContext(Map<String, BeanDefinition> beanDefinitions) {
        this.createBeans(beanDefinitions);
    }

    public void createBeans(Map<String, BeanDefinition> beanDefinitions) {
            beanFactory.createBeans(beanMap, beanDefinitions);
    }

    public void setBeanMap(Map<String, Bean> beanMap) {
        super.beanMap.putAll(beanMap);
    }
}
