package com.k0s.entity;

import com.k0s.annotation.Inject;
import lombok.Getter;

@Getter
//@Setter
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
