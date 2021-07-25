package com.example.datastructor.spring_week05.school_starter;

import java.util.List;

public class MySchool {

    private List<MyClass> myClasseList;

    public MySchool(List<MyClass> myClasseList){
        this.myClasseList = myClasseList;
    }

    @Override
    public String toString(){
        return "MyClass: "+myClasseList.toString();
    }
}
