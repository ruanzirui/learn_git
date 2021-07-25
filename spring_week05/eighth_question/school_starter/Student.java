package com.example.datastructor.spring_week05.school_starter;

import java.util.HashMap;
import java.util.Map;

public class Student {

    private int id;
    private String name;

    public Student(Integer integer, String s){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString(){
        Map<String,Object> map = new HashMap<>(16);
        map.put("id",id);
        map.put("name",name);
        return "Student:"+map.toString();
    }
}
