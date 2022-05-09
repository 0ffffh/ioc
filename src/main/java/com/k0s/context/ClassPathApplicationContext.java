package com.k0s.context;


import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.reader.BeanDefinitionReader;
import com.k0s.reader.stax.XMLBeanDefinitionReader;


import java.util.*;



public class ClassPathApplicationContext extends Context {

    public ClassPathApplicationContext() {
    }

    public ClassPathApplicationContext(String path) {
        BeanDefinitionReader beanDefinitionReader = new XMLBeanDefinitionReader(path);
        this.createBeans(beanDefinitionReader.getBeanDefinitionMap());
    }

    public ClassPathApplicationContext(Map<String, BeanDefinition> beanDefinitions) {
        this.createBeans(beanDefinitions);
    }

    public void createBeans(Map<String, BeanDefinition> beanDefinitions){
        Optional<BeanFactory> beanFactoryOptional = Optional.ofNullable(this.beanFactory);
        if(beanFactoryOptional.isPresent()){
            beanFactory.createBeans(beanMap, beanDefinitions);
        } else {
            beanFactory = new BeanFactory();
            beanFactory.createBeans(beanMap, beanDefinitions);
        }

    }

    public void setBeanMap(Map<String, Bean> beanMap) {
        super.beanMap.putAll(beanMap);
    }
}
