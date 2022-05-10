package com.k0s.entity;


import com.k0s.annotation.Autowired;
import com.k0s.annotation.PostConstruct;
import com.k0s.annotation.Component;
import com.k0s.annotation.Inject;

import java.util.Properties;

@Component(beanId = "mailServicePOP")
public class MailService implements IMailService {
    @Autowired
    private Properties properties;

    @Autowired
    private User user;
    @Inject(value = "POP")
    private String protocol;
    @Inject(value = "555")
    private int port;


    @PostConstruct
    private void init() {
        port = port * 2;
        // make some initialization
        // fill cache
    }

    @Override
    public void sendEmail(User user, String message) {
        System.out.println("sending email with message: " + message + " to " + user.getName() + "protocol " + protocol);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }
}
