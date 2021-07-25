package com.example.datastructor.bean_test;

import com.example.datastructor.spring_week05.XmlBeanExample;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XmlBeanExampleTest {

    @Test
    public void XmlBeanExampleTest(){

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("BeanXml.xml");
        System.out.println(context);
        XmlBeanExample xmlBeanExample = (XmlBeanExample) context.getBean("XmlBeanExample");
        xmlBeanExample.info();
    }

}
