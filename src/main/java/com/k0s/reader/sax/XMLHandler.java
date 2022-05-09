package com.k0s.reader.sax;

import com.k0s.entity.BeanDefinition;
import com.k0s.exception.ParseContextException;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XMLHandler extends DefaultHandler {
    private static final String BEAN = "bean";
    private static final String CLASS = "class";
    private static final String NAME = "name";
    private static final String PROPERTY = "property";
    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String REF = "ref";
    private BeanDefinition beanDefinition;
    @Getter
    private Map<String, BeanDefinition> beanDefinitionMap;
    private Map<String, String> valueDependencies;
    private Map<String, String> refDependencies;
    private boolean isBean;


    @Override
    public void startDocument() {
        beanDefinitionMap = new ConcurrentHashMap<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (BEAN.equalsIgnoreCase(qName)) {
            isBean = true;

            String id = attributes.getValue(ID);
            if (id == null) {
                throw new ParseContextException("No specified id for bean");
            }

            String clazzName = attributes.getValue(CLASS);
            if (clazzName == null) {
                throw new ParseContextException("No specified class for bean");
            }

            beanDefinition = new BeanDefinition(id, clazzName);
            valueDependencies = new HashMap<>(1);
            refDependencies = new HashMap<>(1);
        }
        if (PROPERTY.equalsIgnoreCase(qName)) {
            if (!isBean) {
                throw new ParseContextException("No specified bean for property");
            }

            String name = attributes.getValue(NAME);
            if (name == null) {
                throw new ParseContextException("No specified name for property");
            }

            String value = attributes.getValue(VALUE);
            if (value != null) {
                valueDependencies.put(name, value);
            }

            String ref = attributes.getValue(REF);
            if (ref != null) {
                refDependencies.put(name, ref);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase(BEAN)) {
            isBean = false;
            beanDefinition.setValueDependencies(valueDependencies);
            beanDefinition.setRefDependencies(refDependencies);

            beanDefinitionMap.put(beanDefinition.getId(), beanDefinition);
        }
    }
}
