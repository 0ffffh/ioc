package com.k0s.context;

import com.k0s.annotation.Autowired;
import com.k0s.annotation.Component;
import com.k0s.annotation.Inject;
import com.k0s.annotation.PostConstruct;
import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.exception.CreateBeanException;
import com.k0s.exception.NoUniqBeanException;
import com.k0s.exception.PostConstructException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class BeanFactory {

    private Reflections scanner;
    private final Map<Class<?>, Object> interfaceToImplementation = new ConcurrentHashMap<>();


    public BeanFactory() {
    }


    public void createBeans(Map<String, Bean> beanMap, String basePackage) {
        this.scanner = new Reflections(basePackage);
        Set<Class<?>> types = scanner.getTypesAnnotatedWith(Component.class);
        for (Class<?> clazz : types) {
            Component component = clazz.getAnnotation(Component.class);
            String beanId;
            if (!component.beanId().isEmpty()) {
                beanId = component.beanId();
            } else {
                beanId = getBeanId(clazz);
            }
            beanMap.put(beanId, new Bean(beanId, createBean(clazz)));
        }
        log.info("Created {} beans: {}", beanMap.size(), beanMap.keySet());


    }

    public <T> T createBean(Class<T> clazz) {
        T beanInstance = getBeanInstance(clazz);
        injectAnnotatedValues(clazz, beanInstance);
        injectAnnotatedRefs(clazz, beanInstance);
        postConstruct(beanInstance);
        return beanInstance;
    }


    public void createBeans(Map<String, Bean> beanMap, Map<String, BeanDefinition> beanDefinitions) {
        if (beanDefinitions.isEmpty()) {
            log.info("BeanDefinition map is empty");
            throw new NoSuchElementException("BeanDefinition map is empty");
        }
        beanMap.putAll(beanDefinitions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, beanDefinition ->
                        createBean(beanDefinition.getValue()))));

        injectValueDependencies(beanDefinitions, beanMap);
        injectRefDependencies(beanDefinitions, beanMap);
        postConstruct(beanMap, beanDefinitions);
        log.info("Created {} beans: {}", beanMap.size(), beanMap.keySet());

    }

    public Bean createBean(BeanDefinition beanDefinition) {
        Object classObject;
        try {
            Class<?> clazz = Class.forName(beanDefinition.getClassName());
            classObject = getBeanInstance(clazz);
        } catch (Exception e) {
            log.info("Bean {} not created", beanDefinition.getClassName());
            throw new CreateBeanException("Can't create bean", e);
        }
        return new Bean(beanDefinition.getId(), classObject);
    }


    void injectValueDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        beans.forEach((key, value) -> {
            if (beanDefinitions.containsKey(key)) {
                Optional<Map<String, String>> valueDependencies = Optional.ofNullable(beanDefinitions.get(key).getValueDependencies());
                valueDependencies.ifPresent(valuesMap ->
                        injectValueDependenciesBean(value.getBeanInstance(), valuesMap));
            }
        });
    }

    void injectRefDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        beans.forEach((key, value) -> {
            if (beanDefinitions.containsKey(key)) {
                Optional<Map<String, String>> refDependencies = Optional.ofNullable(beanDefinitions.get(key).getRefDependencies());
                refDependencies.ifPresent(refsMap ->
                        injectRefDependenciesBean(value.getBeanInstance(), refsMap, beans));
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


    @SneakyThrows
    private <T> T getBeanInstance(Class<T> clazz) {
        if (interfaceToImplementation.containsKey(clazz)) {
            return clazz.cast(interfaceToImplementation.get(clazz));
        }

        Class<? extends T> implementationClass = clazz;

        if (implementationClass.isInterface()) {
            implementationClass = getImplementationClass(implementationClass);
        }
        T beanInstance = implementationClass.getDeclaredConstructor().newInstance();
        interfaceToImplementation.put(clazz, beanInstance);

        return beanInstance;
    }

    @SneakyThrows
    private <T> void injectAnnotatedValues(Class<? extends T> implementationClass, T beanInstance) {
        List<Field> fieldList = Arrays.stream(implementationClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class)).toList();

        for (Field field : fieldList) {
            field.setAccessible(true);
            Optional<Inject> inject = Optional.ofNullable(field.getAnnotation(Inject.class));
            if (inject.isPresent()) {
                field.set(beanInstance, toObject(field.getType(), inject.get().value()));
            }
        }
    }

    @SneakyThrows
    private <T> void injectAnnotatedRefs(Class<? extends T> implementationClass, T beanInstance) {
        List<Field> reflist = Arrays.stream(implementationClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class)).toList();

        for (Field field : reflist) {
            field.setAccessible(true);
//            field.set(beanInstance, getBeanInstance((field.getType())));
            field.set(beanInstance, createBean((field.getType())));
        }
    }

    private <T> void postConstruct(T beanInstance) {
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
    }

    private String getBeanId(Class<?> clazz) {
        String name = clazz.getName().substring(clazz.getPackageName().length() + 1);
        String s1 = name.substring(0, 1).toLowerCase();
        return s1 + name.substring(1);
    }

    private <T> Class<? extends T> getImplementationClass(Class<T> interfaceClass) {
        Optional<Reflections> reflections = Optional.ofNullable(scanner);
        if (reflections.isEmpty()) {
            scanner = new Reflections(interfaceClass.getPackageName());
        }

        Set<Class<? extends T>> implementationClasses = scanner.getSubTypesOf(interfaceClass);
        if (implementationClasses.size() != 1) {
            throw new NoUniqBeanException("Interface " + interfaceClass.getCanonicalName() + " has 0 or more then 1 realization");
        }
        return implementationClasses.stream().findFirst().get();
    }

    private static Object toObject(Class<?> clazz, String value) {
        if (Boolean.class == clazz || Boolean.TYPE == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || Byte.TYPE == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || Short.TYPE == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || Integer.TYPE == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || Long.TYPE == clazz) return Long.parseLong(value);
        if (Float.class == clazz || Float.TYPE == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || Double.TYPE == clazz) return Double.parseDouble(value);
        return value;
    }


}
