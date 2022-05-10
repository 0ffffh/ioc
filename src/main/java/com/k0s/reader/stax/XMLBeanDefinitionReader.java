package com.k0s.reader.stax;

import com.k0s.entity.BeanDefinition;
import com.k0s.exception.ParseContextException;
import com.k0s.reader.BeanDefinitionReader;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XMLBeanDefinitionReader implements BeanDefinitionReader {
    private static final String BEAN = "bean";
    private static final String CONTEXT = "context";
    private static final String BASE_PACKAGE = "base-package";
    private static final String CLASS = "class";
    private static final String NAME = "name";
    private static final String PROPERTY = "property";
    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String REF = "ref";
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private String basePackage;

    public XMLBeanDefinitionReader() {
    }

    public XMLBeanDefinitionReader(String path) {
        parseXml(getClass().getClassLoader().getResourceAsStream(path));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    public Map<String, BeanDefinition> getBeanDefinitionMap(InputStream inputStream) {
        parseXml(inputStream);
        return beanDefinitionMap;
    }

    public String getBasePackage() {
        return basePackage;
    }

    @SneakyThrows
    private void parseXml(InputStream inputStream) {
        try (StaxProcessor processor = new StaxProcessor(inputStream)) {

            while (processor.startElement(BEAN, BEAN)) {
                BeanDefinition beanDefinition = parseBeanDefinition(processor);

                while (processor.startElement(PROPERTY, BEAN)) {
                    injectProperties(processor, beanDefinition);
                }

                beanDefinitionMap.put(beanDefinition.getId(), beanDefinition);
            }
        }
    }


    @SneakyThrows
    private BeanDefinition parseBeanDefinition(StaxProcessor processor) {
        String id = processor.getAttribute(ID);
        if (id == null) {
            throw new ParseContextException("No specified id for bean");
        }

        String clazzName = processor.getAttribute(CLASS);
        if (clazzName == null) {
            throw new ParseContextException("No specified class for bean");
        }

        String initMethod = processor.getAttribute("init-method");

        return new BeanDefinition(id, clazzName,
                new HashMap<>(1),
                new HashMap<>(1),
                initMethod);
    }

    @SneakyThrows
    private void injectProperties(StaxProcessor processor, BeanDefinition beanDefinition) {
        String name = processor.getAttribute(NAME);
        if (name == null) {
            throw new ParseContextException("No specified name for property");
        }

        String value = processor.getAttribute(VALUE);
        String ref = processor.getAttribute(REF);

        if (value != null) {
            beanDefinition.getValueDependencies().put(name, value);
        }
        if (ref != null) {
            beanDefinition.getRefDependencies().put(name, ref);
        }
    }
}


