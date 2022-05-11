package com.k0s.context;

import com.k0s.entity.*;
import com.k0s.exception.CreateBeanException;
import com.k0s.exception.NoSuchBeanDefinitionException;
import com.k0s.exception.NoUniqBeanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathApplicationContextTest {


    @Test
    void getBean() {
        ClassPathApplicationContext context = new ClassPathApplicationContext("context.xml");

        context.setBeanFactory(new BeanFactory());

        MailService mailService = (MailService) context.getBean("mailServiceIMAP");
        assertNotNull(mailService);
        assertEquals("IMAP", mailService.getProtocol());
        assertEquals(286, mailService.getPort());

        User user = (User) context.getBean("user");
        assertNotNull(user);


        User user2 = context.getBean("user", User.class);
        assertNotNull(user2);

        User user3 = context.getBean("user2", User.class);
        assertNotNull(user3);
        assertEquals("Ivan", user3.getName());
        assertEquals(20, user3.getAge());

    }


    @Test
    public void testCreateBeans() {

        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
        BeanDefinition beanDefinitionMailService = new BeanDefinition("mailServicePOP", "com.k0s.entity.MailService");
        beanDefinitionMap.put("mailServicePOP", beanDefinitionMailService);
        BeanDefinition beanDefinitionUserService = new BeanDefinition("userService", "com.k0s.entity.DefaultUserService");
        beanDefinitionMap.put("userService", beanDefinitionUserService);

        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext(beanDefinitionMap);



        Map<String, Bean> beanMap = classPathApplicationContext.getBeanMap();

        Bean actualMailBean = beanMap.get("mailServicePOP");
        assertNotNull(actualMailBean);
        assertEquals("mailServicePOP", actualMailBean.getId());
        assertEquals(MailService.class, actualMailBean.getBeanInstance().getClass());

        Bean actualUserBean = beanMap.get("userService");
        assertNotNull(actualUserBean);
        assertEquals("userService", actualUserBean.getId());
        assertEquals(DefaultUserService.class, actualUserBean.getBeanInstance().getClass());
    }


    @Test()
    public void testCreateBeansWithWrongClass() {

        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
        BeanDefinition errorBeanDefinition = new BeanDefinition("mailServicePOP", "com.k0s.entity.TestClass");
        beanDefinitionMap.put("mailServicePOP", errorBeanDefinition);

        assertThrows(CreateBeanException.class,
                ()-> new ClassPathApplicationContext(beanDefinitionMap));


    }

    @Test
    public void testGetBeanById() {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext();
        Map<String, Bean> beanMap = new HashMap<>();
        DefaultUserService beanValue1 = new DefaultUserService();
        DefaultUserService beanValue2 = new DefaultUserService();
        beanMap.put("bean1", new Bean("bean1", beanValue1));
        beanMap.put("bean2", new Bean("bean2", beanValue2));
        classPathApplicationContext.setBeanMap(beanMap);
        DefaultUserService actualBeanValue1 = (DefaultUserService) classPathApplicationContext.getBean("bean1");
        DefaultUserService actualBeanValue2 = (DefaultUserService) classPathApplicationContext.getBean("bean2");
        assertNotNull(actualBeanValue1);
        assertNotNull(actualBeanValue2);
        assertEquals(beanValue1, actualBeanValue1);
        assertEquals(beanValue2, actualBeanValue2);
    }

    @Test
    public void testGetBeanByClazz() {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext();

        Map<String, Bean> beanMap = new HashMap<>();
        DefaultUserService beanValue1 = new DefaultUserService();
        MailService beanValue2 = new MailService();
        beanMap.put("bean1", new Bean("bean1", beanValue1));
        beanMap.put("bean2", new Bean("bean2", beanValue2));
        classPathApplicationContext.setBeanMap(beanMap);
        DefaultUserService actualBeanValue1 = classPathApplicationContext.getBean(DefaultUserService.class);
        MailService actualBeanValue2 = classPathApplicationContext.getBean(MailService.class);
        assertNotNull(actualBeanValue1);
        assertNotNull(actualBeanValue2);
        assertEquals(beanValue1, actualBeanValue1);
        assertEquals(beanValue2, actualBeanValue2);
    }

    @Test
    public void testGetBeanByClazzNoUniqueBean() {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext();
        Map<String, Bean> beanMap = new HashMap<>();
        beanMap.put("bean1", new Bean("bean1", new DefaultUserService()));
        beanMap.put("bean2", new Bean("bean2", new DefaultUserService()));
        classPathApplicationContext.setBeanMap(beanMap);

        assertThrows(NoUniqBeanException.class,
                ()-> classPathApplicationContext.getBean(DefaultUserService.class));
    }

    @Test
    public void testGetBeanByIdAndClazz() {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext();
        Map<String, Bean> beanMap = new HashMap<>();
        DefaultUserService beanValue1 = new DefaultUserService();
        DefaultUserService beanValue2 = new DefaultUserService();
        beanMap.put("bean1", new Bean("bean1", beanValue1));
        beanMap.put("bean2", new Bean("bean2", beanValue2));
        classPathApplicationContext.setBeanMap(beanMap);
        DefaultUserService actualBeanValue1 = classPathApplicationContext.getBean("bean1", DefaultUserService.class);
        DefaultUserService actualBeanValue2 = classPathApplicationContext.getBean("bean2", DefaultUserService.class);
        assertNotNull(actualBeanValue1);
        assertNotNull(actualBeanValue2);
        assertEquals(beanValue1, actualBeanValue1);
        assertEquals(beanValue2, actualBeanValue2);
    }


    @Test
    public void testGetBeanByIdAndClazzNoSuchBean() {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext();
        Map<String, Bean> beanMap = new HashMap<>();
        DefaultUserService beanValue = new DefaultUserService();
        beanMap.put("bean1", new Bean("bean1", beanValue));
        classPathApplicationContext.setBeanMap(beanMap);
        assertThrows(NoSuchBeanDefinitionException.class,
                ()-> classPathApplicationContext.getBean("bean1", MailService.class));

    }

    @Test
    public void getBeanNames() {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext();
        Map<String, Bean> beanMap = new HashMap<>();
        beanMap.put("bean3", new Bean("bean3", new DefaultUserService()));
        beanMap.put("bean4", new Bean("bean4", new DefaultUserService()));
        beanMap.put("bean5", new Bean("bean5", new DefaultUserService()));
        classPathApplicationContext.setBeanMap(beanMap);
        List<String> actualBeansNames = classPathApplicationContext.getBeanNames();
        List<String> expectedBeansNames = Arrays.asList("bean3", "bean4", "bean5");
        assertTrue(actualBeansNames.containsAll(expectedBeansNames));
        assertTrue(expectedBeansNames.containsAll(actualBeansNames));
    }


    @Test
    public void testCreateBeansFromXML()  {
        ClassPathApplicationContext classPathApplicationContext = new ClassPathApplicationContext("context.xml");
        Map<String, Bean> beanMap = classPathApplicationContext.getBeanMap();
        System.out.println(beanMap.toString());
        Bean bean = beanMap.get("mailServicePOP");
        System.out.println(bean.getId());
        assertEquals("com.k0s.entity.MailService", bean.getBeanInstance().getClass().getName());

        Bean bean1 = beanMap.get("userServiceImap");
        UserService userService = (DefaultUserService) bean1.getBeanInstance();
        userService.activateUsers();
        System.out.println(userService.getMailService().getClass());

        Bean bean2 = beanMap.get("userService");
        UserService userService1 = classPathApplicationContext.getBean("userService", DefaultUserService.class);
        userService1.activateUsers();

        User user = (User) classPathApplicationContext.getBean("user");
        System.out.println(user.getClass());
        System.out.println(user.getName());
        System.out.println(user.getAge());

        User user1 = (User) classPathApplicationContext.getBean("user2");
        System.out.println(user1.getName());
        System.out.println(user1.getAge());

        MailService mailService = (MailService) beanMap.get("mailServiceIMAP").getBeanInstance();
        MailService mailService2 = (MailService) beanMap.get("mailServicePOP").getBeanInstance();

        System.out.println(mailService.getPort());
        System.out.println(mailService2.getPort());

    }
}