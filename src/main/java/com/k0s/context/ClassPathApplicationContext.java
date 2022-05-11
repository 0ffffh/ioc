package com.k0s.context;


import com.k0s.beanFactory.BeanFactory;
import com.k0s.beanFactory.ClassPathBeanFactory;
import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.reader.BeanDefinitionReader;
import com.k0s.reader.stax.XMLBeanDefinitionReader;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class ClassPathApplicationContext extends Context {
    @Setter
    protected ClassPathBeanFactory beanFactory;


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
        Optional<BeanFactory> beanFactoryOptional = Optional.ofNullable(this.beanFactory);
        if (beanFactoryOptional.isPresent()) {
            beanFactory.createBeans(beanMap, beanDefinitions);
        } else {
            beanFactory = new ClassPathBeanFactory();
            beanFactory.createBeans(beanMap, beanDefinitions);
        }

    }

    public void setBeanMap(Map<String, Bean> beanMap) {
        super.beanMap.putAll(beanMap);
    }
}
