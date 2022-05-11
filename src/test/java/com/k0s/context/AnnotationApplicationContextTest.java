package com.k0s.context;

import com.k0s.entity.Bean;
import com.k0s.entity.DefaultUserService;
import com.k0s.entity.MailService;
import com.k0s.entity.User;
import com.k0s.exception.NoSuchBeanDefinitionException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationApplicationContextTest {

    @Test
    void getBean() {
        AnnotationApplicationContext context = new AnnotationApplicationContext("com.k0s");


        MailService mailService = (MailService) context.getBean("mailServicePOP");
        assertNotNull(mailService);
        assertEquals("POP", mailService.getProtocol());
        assertEquals(1110, mailService.getPort());

        User user = (User) context.getBean("user");
        assertNotNull(user);


        User user2 = context.getBean("user", User.class);
        assertNotNull(user2);


        for (String beanName : context.getBeanNames()) {
            System.out.println(beanName);
        }

        assertThrows(NoSuchBeanDefinitionException.class,  ()->{
            User user3 = context.getBean("user2", User.class);
        });

    }


    @Test
    public void testCreateBeans() {

        AnnotationApplicationContext context = new AnnotationApplicationContext("com.k0s");
        context.getBean("mailServicePOP");
        context.getBean("userService");


        Map<String, Bean> beanMap = context.getBeanMap();

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
    public void testGetBeanByIdAndClazzNoSuchBean() {
        AnnotationApplicationContext context = new AnnotationApplicationContext("com.k0s");
        assertThrows(NoSuchBeanDefinitionException.class,
                ()-> context.getBean("bean1", MailService.class));

    }

    @Test
    public void getBeanNames() {
        AnnotationApplicationContext context = new AnnotationApplicationContext("com.k0s");

        List<String> actualBeansNames = context.getBeanNames();


        List<String> expectedBeansNames = Arrays.asList("mailServicePOP", "user", "userService");
        assertTrue(actualBeansNames.containsAll(expectedBeansNames));
        assertTrue(expectedBeansNames.containsAll(actualBeansNames));
    }

}