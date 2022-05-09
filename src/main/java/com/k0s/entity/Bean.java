package com.k0s.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Bean {
    private String id;
    private Object beanInstance;
}
