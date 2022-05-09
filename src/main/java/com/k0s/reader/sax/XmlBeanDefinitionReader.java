package com.k0s.reader.sax;

import com.k0s.entity.BeanDefinition;
import com.k0s.reader.BeanDefinitionReader;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class XmlBeanDefinitionReader implements BeanDefinitionReader {
    private String path;


    @SneakyThrows
    Map<String, BeanDefinition> getBeanDefinitionMap(InputStream inputStream) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        XMLHandler handler = new XMLHandler();
        parser.parse(inputStream, handler);
        return handler.getBeanDefinitionMap();
    }


    public Map<String, BeanDefinition> getBeanDefinition(String path){
        return getBeanDefinitionMap(getClass().getClassLoader().getResourceAsStream(path));
    }
    @Override
    @SneakyThrows
    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return getBeanDefinitionMap(getClass().getClassLoader().getResourceAsStream(this.path));
    }
}
