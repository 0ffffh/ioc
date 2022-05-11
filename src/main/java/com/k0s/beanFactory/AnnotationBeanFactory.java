package com.k0s.beanFactory;

import com.k0s.annotation.Autowired;
import com.k0s.annotation.Component;
import com.k0s.annotation.Inject;
import com.k0s.annotation.PostConstruct;
import com.k0s.entity.Bean;
import com.k0s.exception.PostConstructException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class AnnotationBeanFactory extends BeanFactory {


    public AnnotationBeanFactory() {
    }


    public void createBeans(Map<String, Bean> beanMap, String basePackage) {
        super.scanner = new Reflections(basePackage);
        Set<Class<?>> types = scanner.getTypesAnnotatedWith(Component.class);
        for (Class<?> clazz : types) {
            Component component = clazz.getAnnotation(Component.class);
            String beanId;
            if (!component.beanId().isEmpty()) {
                beanId = component.beanId();
            } else {
                beanId = super.getBeanId(clazz);
            }
            beanMap.put(beanId, new Bean(beanId, createBean(clazz)));
        }
        injectAnnotatedValues(beanMap);
        injectAnnotatedRefs(beanMap);
        postConstruct(beanMap);
        log.info("Created {} beans: {}", beanMap.size(), beanMap.keySet());


    }


    @SneakyThrows
    public <T> T createBean(Class<T> clazz) {
        if (interfaceToImplementation.containsKey(clazz)) {
            return clazz.cast(interfaceToImplementation.get(clazz));
        }

        Class<? extends T> implementationClass = clazz;

        if (implementationClass.isInterface()) {
            implementationClass = super.getImplementationClass(implementationClass);
        }
        T beanInstance = implementationClass.getDeclaredConstructor().newInstance();
        interfaceToImplementation.put(clazz, beanInstance);

        return beanInstance;
    }


    private <T> void injectAnnotatedValues(Map<String, Bean> beanMap) {
        beanMap.values().forEach(bean -> {
            Object beanInstance = bean.getBeanInstance();
            Class<?> implementationClass = beanInstance.getClass();
            List<Field> fieldList = Arrays.stream(implementationClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Inject.class)).toList();

            for (Field field : fieldList) {
                field.setAccessible(true);
                Optional<Inject> inject = Optional.ofNullable(field.getAnnotation(Inject.class));
                if (inject.isPresent()) {
                    try {
                        field.set(beanInstance, toObject(field.getType(), inject.get().value()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }

    @SneakyThrows
    private <T> void injectAnnotatedRefs(Map<String, Bean> beanMap) {
        beanMap.values().forEach(bean -> {
            Object beanInstance = bean.getBeanInstance();
            Class<?> implementationClass = beanInstance.getClass();
            List<Field> reflist = Arrays.stream(implementationClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Autowired.class)).toList();

            for (Field field : reflist) {
                field.setAccessible(true);
                if (beanMap.containsKey(field.getName())) {
                    try {
                        field.set(beanInstance, beanMap.get(field.getName()).getBeanInstance());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }

    private <T> void postConstruct(Map<String, Bean> beanMap) {
        beanMap.values().forEach(bean -> {
            Object beanInstance = bean.getBeanInstance();
            for (Method declaredMethod : beanInstance.getClass().getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(PostConstruct.class)) {
                    try {
                        declaredMethod.setAccessible(true);
                        declaredMethod.invoke(beanInstance);
                    } catch (Exception e) {
                        throw new PostConstructException(declaredMethod.getName(), e);
                    }
                }
            }
        });
    }
}
