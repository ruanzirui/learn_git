package com.example.datastructor.spring_week05.school_starter;

import java.util.ArrayList;
import java.util.List;

public class MyClass {

    private int id;
    private String name;

    public MyClass(Integer integer, String s){
        this.id = id;
        this.name = name;
    }

    List<Student> studentList = new ArrayList<>();
    public void addStudent(Student student){
        studentList.add(student);
    }

    @Override
    public String toString(){
        return "MyClass: "+ studentList.toString();
    }

}
