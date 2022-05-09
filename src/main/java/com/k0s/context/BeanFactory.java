package com.k0s.context;

import com.k0s.annotation.Autowired;
import com.k0s.annotation.Service;
import com.k0s.annotation.Inject;
import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.exception.CreateBeanException;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BeanFactory {

    private final Map<Class, Object> interfaceToImplementation = new ConcurrentHashMap<>();


    private Reflections scanner;

    public BeanFactory() {
    }

    public void createBeans(Map<String, Bean> beanMap, String basePackage) {
        this.scanner = new Reflections(basePackage);
        Set<Class<?>> types = scanner.getTypesAnnotatedWith(Service.class);
        for (Class<?> clazz : types) {

            Service service = clazz.getAnnotation(Service.class);
            String beanId;
            if (!service.beanId().isEmpty()) {
                beanId = service.beanId();
            } else {
                beanId = getBeanId(clazz);
            }
            beanMap.put(beanId, new Bean(beanId, createBean(clazz)));
        }
    }


    public void createBeans(Map<String, Bean> beanMap, Map<String, BeanDefinition> beanDefinitions) {
        if (beanDefinitions.isEmpty()) {
            throw new NoSuchElementException("BeanDefinition map is empty");
        }
        beanMap.putAll(beanDefinitions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, beanDefinition ->
                        createBean(beanDefinition.getValue()))));

        injectValueDependencies(beanDefinitions, beanMap);
        injectRefDependencies(beanDefinitions, beanMap);

    }


    public Bean createBean(BeanDefinition beanDefinition) {
        Object classObject;
        try {
            Class<?> clazz = Class.forName(beanDefinition.getClassName());
            classObject = createBean(clazz);
        } catch (Exception e) {
            throw new CreateBeanException("Can't create bean", e);
        }
        return new Bean(beanDefinition.getId(), classObject);
    }


    public <T> T createBean(Class<T> clazz) {
        System.out.println("===create bin===");
        if (interfaceToImplementation.containsKey(clazz)) {
            return clazz.cast(interfaceToImplementation.get(clazz));
        }
        T beanInstance = getBean(clazz);
        interfaceToImplementation.put(clazz, beanInstance);
        return beanInstance;
//        recursive update
//        return clazz.cast(interfaceToImplementation.computeIfAbsent(clazz, bean -> getBean(clazz)));
    }

    @SneakyThrows
    public <T> T getBean(Class<T> clazz) {
        System.out.println("===GET===");

        Class<? extends T> implementationClass = clazz;

        if (implementationClass.isInterface()) {
            System.out.println(implementationClass.getName());
            implementationClass = getImplementationClass(implementationClass);
        }

        T beanInstance = implementationClass.getDeclaredConstructor().newInstance();

//        List<Field> reflist = Arrays.stream(implementationClass.getDeclaredFields())
//                .filter(field -> field.isAnnotationPresent(Autowired.class)).toList();
//
//        for (Field field : reflist) {
//            field.setAccessible(true);
//            field.set(beanInstance, createBean((field.getType())));
//        }


//        List<Field> fieldList = Arrays.stream(implementationClass.getDeclaredFields())
//                .filter(field -> field.isAnnotationPresent(Inject.class)).toList();
//
//        for (Field field : fieldList) {
//            field.setAccessible(true);
//            Optional<Inject> inject = Optional.ofNullable(field.getAnnotation(Inject.class));
//            if (inject.isPresent()) {
//                field.set(beanInstance, toObject(field.getType(), inject.get().value()));
//            }
//        }
        injectAnnotatedValues(implementationClass, beanInstance);
        injectAnnotatedRefs(implementationClass, beanInstance);
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
            field.set(beanInstance, createBean((field.getType())));
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
            throw new RuntimeException("Interface " + interfaceClass.getCanonicalName() + " has 0 or more then 1 realization");
        }
        return implementationClasses.stream().findFirst().get();
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
