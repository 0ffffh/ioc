package com.k0s.beanFactory;

import com.k0s.entity.Bean;
import com.k0s.entity.BeanDefinition;
import com.k0s.exception.CreateBeanException;
import com.k0s.exception.NoUniqBeanException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class FactoryBean implements BeanFactory {

    Reflections scanner;
    final Map<Class<?>, Object> interfaceToImplementation = new ConcurrentHashMap<>();


    @SneakyThrows
    @Override
    public <T> T createBean(Class<T> clazz) {
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

    @Override
    public Bean createBean(BeanDefinition beanDefinition) {
        Object beanInstance;
        try {
            Class<?> clazz = Class.forName(beanDefinition.getClassName());
            beanInstance = createBean(clazz);
        } catch (Exception e) {
            log.info("Bean {} not created", beanDefinition.getClassName());
            throw new CreateBeanException("Can't create bean", e);
        }
        return new Bean(beanDefinition.getId(), beanInstance);
    }


    String getBeanId(Class<?> clazz) {
        String name = clazz.getName().substring(clazz.getPackageName().length() + 1);
        String s1 = name.substring(0, 1).toLowerCase();
        return s1 + name.substring(1);
    }

    <T> Class<? extends T> getImplementationClass(Class<T> interfaceClass) {
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

    static Object toObject(Class<?> clazz, String value) {
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
