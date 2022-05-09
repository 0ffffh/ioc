package com.k0s.context;

import com.k0s.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BeanFactoryTest {
    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        beanFactory = new BeanFactory();
    }

    @Test
    void getBean() {
    }

    @Test
    void createBeanFromDefinition() {
        BeanDefinition beanDefinitionMailService = new BeanDefinition("mailServicePOP", "com.k0s.entity.MailService");
        Bean actualMailBean = beanFactory.createBean(beanDefinitionMailService);

        assertNotNull(actualMailBean);
        assertEquals("mailServicePOP", actualMailBean.getId());
        assertEquals(MailService.class, actualMailBean.getBeanInstance().getClass());
    }

    @Test
    void createBeansFromDefinition() {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
        BeanDefinition beanDefinitionMailService = new BeanDefinition("mailServicePOP", "com.k0s.entity.MailService");
        beanDefinitionMap.put("mailServicePOP", beanDefinitionMailService);
        BeanDefinition beanDefinitionUserService = new BeanDefinition("userService", "com.k0s.entity.DefaultUserService");
        beanDefinitionMap.put("userService", beanDefinitionUserService);

        Map<String, Bean> beanMap = new HashMap<>();
        beanFactory.createBeans(beanMap, beanDefinitionMap);


        Bean actualMailBean = beanMap.get("mailServicePOP");
        assertNotNull(actualMailBean);
        assertEquals("mailServicePOP", actualMailBean.getId());
        assertEquals(MailService.class, actualMailBean.getBeanInstance().getClass());

        Bean actualUserBean = beanMap.get("userService");
        assertNotNull(actualUserBean);
        assertEquals("userService", actualUserBean.getId());
        assertEquals(DefaultUserService.class, actualUserBean.getBeanInstance().getClass());
    }

    @Test
    void createBeanFromAnnotation() {
        User user = beanFactory.createBean(User.class);
        assertEquals("Ivan", user.getName());
        assertEquals(20, user.getAge());

    }

    @Test
    void createBeansFromAnnotation() {
        Map<String, Bean> beanMap = new HashMap<>();
        beanFactory.createBeans(beanMap, "com.k0s");

        Bean actualMailBean = beanMap.get("mailServicePOP");
        assertNotNull(actualMailBean);
        assertEquals("mailServicePOP", actualMailBean.getId());
        assertEquals(MailService.class, actualMailBean.getBeanInstance().getClass());

        Bean actualUserBean = beanMap.get("userService");
        assertNotNull(actualUserBean);
        assertEquals("userService", actualUserBean.getId());
        assertEquals(DefaultUserService.class, actualUserBean.getBeanInstance().getClass());

    }

    @Test
    public void testInjectValueDependencies() {
        Map<String, Bean> beanMap = new HashMap<>();
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

        MailService mailServicePOP = new MailService();
        beanMap.put("mailServicePOP", new Bean("mailServicePOP", mailServicePOP));
        MailService mailServiceIMAP = new MailService();
        beanMap.put("mailServiceIMAP", new Bean("mailServiceIMAP", mailServiceIMAP));

        //  setPort(110) and setProtocol("POP3") via valueDependencies
        BeanDefinition popServiceBeanDefinition = new BeanDefinition("mailServicePOP", "com.study.entity.MailService");
        Map<String, String> popServiceValueDependencies = new HashMap<>();
        popServiceValueDependencies.put("port", "110");
        popServiceValueDependencies.put("protocol", "POP3");
        popServiceBeanDefinition.setValueDependencies(popServiceValueDependencies);
        beanDefinitionMap.put("mailServicePOP", popServiceBeanDefinition);

        //  setPort(143) and setProtocol("IMAP") via valueDependencies
        BeanDefinition imapServiceBeanDefinition = new BeanDefinition("mailServiceIMAP", "com.study.entity.MailService");
        Map<String, String> imapServiceValueDependencies = new HashMap<>();
        imapServiceValueDependencies.put("port", "143");
        imapServiceValueDependencies.put("protocol", "IMAP");
        imapServiceBeanDefinition.setValueDependencies(imapServiceValueDependencies);
        beanDefinitionMap.put("mailServiceIMAP", imapServiceBeanDefinition);

        beanFactory.injectValueDependencies(beanDefinitionMap, beanMap);
        assertEquals(110, mailServicePOP.getPort());
        assertEquals("POP3", mailServicePOP.getProtocol());
        assertEquals(143, mailServiceIMAP.getPort());
        assertEquals("IMAP", mailServiceIMAP.getProtocol());
    }

    @Test
    public void testInjectRefDependencies() {
        Map<String, Bean> beanMap = new HashMap<>();
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

        MailService mailServicePOP = new MailService();
        mailServicePOP.setPort(110);
        mailServicePOP.setProtocol("POP3");
        beanMap.put("mailServicePOP", new Bean("mailServicePOP", mailServicePOP));

        DefaultUserService userService = new DefaultUserService();
        beanMap.put("userService", new Bean("userService", userService));

        //  setMailService(mailServicePOP) via refDependencies
        BeanDefinition userServiceBeanDefinition = new BeanDefinition("userService", "com.study.entity.DefaultUserService");
        Map<String, String> userServiceRefDependencies = new HashMap<>();
        userServiceRefDependencies.put("mailService", "mailServicePOP");
        userServiceBeanDefinition.setRefDependencies(userServiceRefDependencies);
        beanDefinitionMap.put("userService", userServiceBeanDefinition);

        beanFactory.injectRefDependencies(beanDefinitionMap, beanMap);
        assertNotNull(userService.getMailService());
        assertEquals(110, ((MailService) userService.getMailService()).getPort());
        assertEquals("POP3", ((MailService) userService.getMailService()).getProtocol());
    }


}