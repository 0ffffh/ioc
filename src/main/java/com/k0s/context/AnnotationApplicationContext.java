package com.k0s.context;

import com.k0s.context.ApplicationContext;
import com.k0s.context.BeanFactory;
import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnnotationApplicationContext implements ApplicationContext {

    @Setter
    private BeanFactory beanFactory;
    private final Map<String, Bean> beanMap = new ConcurrentHashMap<>();

    public AnnotationApplicationContext(String basePackage) {
        this.createBeans(basePackage);
    }

    public void createBeans(String basePackage){
        Optional<BeanFactory> beanFactoryOptional = Optional.ofNullable(this.beanFactory);
        if(beanFactoryOptional.isPresent()){
            beanFactory.createBeans(beanMap, basePackage);
        } else {
            throw new RuntimeException("BeenFactory not set");
        }

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
            throw new RuntimeException(clazz.getName() + "has 0 or more then 1 realization");
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
        throw new RuntimeException("No bean definition " + "id = " + id + " classname = " + clazz.getName()
                + " bean class = " + beanMap.get(id).getBeanInstance().getClass().getName());

    }

    @Override
    public List<String> getBeanNames() {
        return beanMap.keySet().stream().toList();
    }
}
