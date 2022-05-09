package com.k0s.context;

import com.k0s.entity.Bean;
import com.k0s.exception.NoSuchBeanDefinitionException;
import com.k0s.exception.NoUniqBeanException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Context implements ApplicationContext {

    protected BeanFactory beanFactory;
    protected final Map<String, Bean> beanMap = new ConcurrentHashMap<>();


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
