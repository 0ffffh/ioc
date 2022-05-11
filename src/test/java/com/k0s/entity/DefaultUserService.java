package com.k0s.entity;

import com.k0s.annotation.Autowired;
import com.k0s.annotation.Component;
import com.k0s.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
@Component(beanId = "userService")
public class DefaultUserService implements UserService {

    @Autowired
    private User user;

    @Autowired
    private IMailService mailService;

    @PostConstruct
    public void activateUsers() {
        System.out.println("Get users from db");

        List<User> users = new ArrayList<>(); // userDao.getAll();

        users.add(new User("Ivan"));
        users.add(new User("Petro"));

        for (User user : users) {
            mailService.sendEmail(user, "You are active now");
        }
    }

    public void setMailService(IMailService mailService) {
        this.mailService = mailService;
    }

    public IMailService getMailService() {
        return mailService;
    }
}
