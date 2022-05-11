package com.k0s.beanFactory;

import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.exception.CreateBeanException;
import com.k0s.exception.PostConstructException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ClassPathBeanFactory extends BeanFactory{
    public ClassPathBeanFactory() {
    }

    public void createBeans(Map<String, Bean> beanMap, Map<String, BeanDefinition> beanDefinitions) {
        if (beanDefinitions.isEmpty()) {
            log.info("BeanDefinition map is empty");
            throw new NoSuchElementException("BeanDefinition map is empty");
        }
        beanMap.putAll(beanDefinitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, beanDefinition -> createBean(beanDefinition.getValue()))));

        injectValueDependencies(beanDefinitions, beanMap);
        injectRefDependencies(beanDefinitions, beanMap);
        postConstruct(beanMap, beanDefinitions);
        log.info("Created {} beans: {}", beanMap.size(), beanMap.keySet());

    }

    public Bean createBean(BeanDefinition beanDefinition) {
        Object classObject;
        try {
            Class<?> clazz = Class.forName(beanDefinition.getClassName());
            classObject = createBean(clazz);
        } catch (Exception e) {
            log.info("Bean {} not created", beanDefinition.getClassName());
            throw new CreateBeanException("Can't create bean", e);
        }
        return new Bean(beanDefinition.getId(), classObject);
    }


    public void injectValueDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        beans.forEach((key, value) -> {
            if (beanDefinitions.containsKey(key)) {
                Optional<Map<String, String>> valueDependencies = Optional.ofNullable(beanDefinitions.get(key).getValueDependencies());
                valueDependencies.ifPresent(valuesMap -> injectValueDependenciesBean(value.getBeanInstance(), valuesMap));
            }
        });
    }

    @SneakyThrows
    private void injectValueDependenciesBean(Object beanObject, Map<String, String> valueDependencies) {
        for (Field field : beanObject.getClass().getDeclaredFields()) {
            if (valueDependencies.containsKey(field.getName())) {
                field.setAccessible(true);
                field.set(beanObject, toObject(field.getType(), valueDependencies.get(field.getName())));
            }
        }
    }

    public void injectRefDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        beans.forEach((key, value) -> {
            if (beanDefinitions.containsKey(key)) {
                Optional<Map<String, String>> refDependencies = Optional.ofNullable(beanDefinitions.get(key).getRefDependencies());
                refDependencies.ifPresent(refsMap -> injectRefDependenciesBean(value.getBeanInstance(), refsMap, beans));
            }
        });
    }

    @SneakyThrows
    private void injectRefDependenciesBean(Object beanObject, Map<String, String> refDependencies, Map<String, Bean> beans) {
        for (Field field : beanObject.getClass().getDeclaredFields()) {
            if (refDependencies.containsKey(field.getName())) {
                field.setAccessible(true);
                field.set(beanObject, beans.get(refDependencies.get(field.getName())).getBeanInstance());
            }
        }
    }


    private void postConstruct(Map<String, Bean> beanMap, Map<String, BeanDefinition> beanDefinitions) {
        beanMap.forEach((key, value) -> {
            if (beanDefinitions.containsKey(key)) {
                Optional<String> initMethod = Optional.ofNullable(beanDefinitions.get(key).getInitMethod());
                if (initMethod.isPresent()) {
                    for (Method declaredMethod : value.getBeanInstance().getClass().getDeclaredMethods()) {
                        if (declaredMethod.getName().equals(initMethod.get())) {
                            try {
                                declaredMethod.setAccessible(true);
                                declaredMethod.invoke(value.getBeanInstance());
                            } catch (Exception e) {
                                throw new PostConstructException(declaredMethod.getName(), e);
                            }
                        }
                    }
                }

            }
        });
    }



}
