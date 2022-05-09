package com.k0s.context;


import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.exception.NoSuchBeanDefinitionException;
import com.k0s.exception.NoUniqBeanException;
import com.k0s.reader.BeanDefinitionReader;
import com.k0s.reader.stax.XMLBeanDefinitionReader;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class ClassPathApplicationContext implements ApplicationContext {
    @Setter
    private BeanFactory beanFactory;

    @Getter
    private final Map<String, Bean> beanMap = new ConcurrentHashMap<>();


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
//            throw new RuntimeException("BeenFactory not set");
            beanFactory = new BeanFactory();
            beanFactory.createBeans(beanMap, beanDefinitions);
        }

    }


    public void setBeanMap(Map<String, Bean> beanMap) {
        this.beanMap.putAll(beanMap);
    }


    @Override
    public Object getBean(String beanId) {
        if (beanMap.containsKey(beanId)) {
            return beanMap.get(beanId).getBeanInstance();
        }
        return null;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Set<Bean> beanSet = new HashSet<>(beanMap.values());
        beanSet = beanSet.stream()
                .filter(bean -> clazz.isAssignableFrom(bean.getBeanInstance().getClass()))
                .collect(Collectors.toSet());
        if (beanSet.size() != 1) {
            throw new NoUniqBeanException(clazz.getName());
        }
        return clazz.cast(beanSet.stream().findFirst().get().getBeanInstance());
    }

    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        if (beanMap.containsKey(id)) {
            Bean bean = beanMap.get(id);
            if (clazz.isAssignableFrom(bean.getBeanInstance().getClass())) {
                return clazz.cast(bean.getBeanInstance());
            }
        }
        throw new NoSuchBeanDefinitionException(id, clazz.getName(), beanMap.get(id).getBeanInstance().getClass().getName());

    }

    @Override
    public List<String> getBeanNames() {
        return beanMap.keySet().stream().toList();
    }
}
