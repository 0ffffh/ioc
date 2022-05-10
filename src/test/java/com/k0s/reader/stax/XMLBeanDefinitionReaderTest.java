package com.k0s.reader.stax;

import com.k0s.entity.BeanDefinition;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XMLBeanDefinitionReaderTest {
    private static final String CONTEXT_XML = "<beans>\n" +
            "    <bean id=\"mailServicePOP\" class=\"com.k0s.entity.MailService\">\n" +
            "        <property name=\"port\" value=\"995\"/>\n" +
            "        <property name=\"protocol\" value=\"POP3\"/>\n" +
            "    </bean>\n" +
            "        <property name=\"protocol\" value=\"POP3\"/>\n" +
            "\n" +
            "    <bean id=\"userService\" class=\"com.k0s.entity.DefaultUserService\">\n" +
            "        <property name=\"mailService\" ref=\"mailServicePOP\"/>\n" +
            "    </bean>\n" +
            "\n" +
            "    <bean id=\"mailServiceIMAP\" class=\"com.k0s.entity.MailService\">\n" +
            "        <property name=\"port\" value=\"143\"/>\n" +
            "        <property name=\"protocol\" value=\"IMAP\"/>\n" +
            "    </bean>\n" +
            "</beans>";

    @Test
    void getBeanDefinition() {

        XMLBeanDefinitionReader XMLBeanDefinitionReader = new XMLBeanDefinitionReader();
        Map<String, BeanDefinition> beanDefinitionMap = XMLBeanDefinitionReader.getBeanDefinitionMap(new ByteArrayInputStream(CONTEXT_XML.getBytes()));


        assertEquals(3, beanDefinitionMap.size());
        BeanDefinition beanDefinition1 = beanDefinitionMap.get("mailServicePOP");
        assertEquals("mailServicePOP", beanDefinition1.getId());
        assertEquals("com.k0s.entity.MailService", beanDefinition1.getClassName());

        assertTrue(beanDefinition1.getRefDependencies().isEmpty());
        Map<String, String> valueDependencies1 = beanDefinition1.getValueDependencies();
        assertEquals(2, valueDependencies1.size());
        assertTrue(valueDependencies1.containsKey("port"));
        assertEquals("995", valueDependencies1.get("port"));
        assertTrue(valueDependencies1.containsKey("protocol"));
        assertEquals("POP3", valueDependencies1.get("protocol"));

        BeanDefinition beanDefinition2 = beanDefinitionMap.get("userService");
        assertEquals("userService", beanDefinition2.getId());
        assertEquals("com.k0s.entity.DefaultUserService", beanDefinition2.getClassName());

        assertTrue(beanDefinition2.getValueDependencies().isEmpty());
        Map<String, String> refDependencies2 = beanDefinition2.getRefDependencies();
        assertEquals(1, refDependencies2.size());
        assertTrue(refDependencies2.containsKey("mailService"));
        assertEquals("mailServicePOP", refDependencies2.get("mailService"));

        BeanDefinition beanDefinition3 = beanDefinitionMap.get("mailServiceIMAP");
        assertEquals("mailServiceIMAP", beanDefinition3.getId());
        assertEquals("com.k0s.entity.MailService", beanDefinition3.getClassName());

        assertTrue(beanDefinition3.getRefDependencies().isEmpty());
        Map<String, String> valueDependencies3 = beanDefinition3.getValueDependencies();
        assertEquals(2, valueDependencies3.size());
        assertTrue(valueDependencies3.containsKey("port"));
        assertEquals("143", valueDependencies3.get("port"));
        assertTrue(valueDependencies3.containsKey("protocol"));
        assertEquals("IMAP", valueDependencies3.get("protocol"));
    }


}