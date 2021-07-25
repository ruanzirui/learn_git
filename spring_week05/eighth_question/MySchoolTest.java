package com.example.datastructor.bean_test;

import com.example.datastructor.spring_week05.school_starter.MySchool;
import com.example.datastructor.spring_week05.school_starter.SchoolAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SchoolAutoConfiguration.class)
public class MySchoolTest {

    @Autowired
    MySchool mySchool;

    @Test
    public void test(){
        System.out.println(mySchool.toString());
    }

}
