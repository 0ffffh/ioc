package com.k0s.entity;

import com.k0s.annotation.Autowired;
import com.k0s.annotation.Inject;
import com.k0s.annotation.Component;
import lombok.Getter;

@Getter
@Component
public class User {
    @Inject(value = "Ivan")
    private String name;
    @Inject(value = "20")
    private int age;

    public User(){}


    public User(String name){
        this.name = name;
    }

}
