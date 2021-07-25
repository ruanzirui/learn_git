package com.example.datastructor.spring_week05;

import org.springframework.stereotype.Component;

@Component
public class BeanExample {

    public BeanExample(){
        System.out.println("Hello World");
    }

    public void info(){
        System.out.println("Auto Hello World");
    }

}
