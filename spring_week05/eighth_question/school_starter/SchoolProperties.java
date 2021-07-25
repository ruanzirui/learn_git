package com.example.datastructor.spring_week05.school_starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "school")
public class SchoolProperties {

    private List<Integer> studentIds;
    private List<String> studentNames;
    private List<Integer> myClassIds;
    private List<String> myClassNames;
    private List<Map<String, Integer>> studentOfClass;

    public List<Integer> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Integer> studentIds) {
        this.studentIds = studentIds;
    }

    public List<String> getStudentNames() {
        return studentNames;
    }

    public void setStudentNames(List<String> studentNames) {
        this.studentNames = studentNames;
    }

    public List<Integer> getMyClassIds() {
        return myClassIds;
    }

    public void setMyClassIds(List<Integer> myClassIds) {
        this.myClassIds = myClassIds;
    }

    public List<String> getMyClassNames() {
        return myClassNames;
    }

    public void setMyClassNames(List<String> myClassNames) {
        this.myClassNames = myClassNames;
    }


    public List<Map<String, Integer>> getStudentOfClass() {
        return studentOfClass;
    }

    public void setStudentOfClass(List<Map<String, Integer>> studentOfClass) {
        this.studentOfClass = studentOfClass;
    }
}
