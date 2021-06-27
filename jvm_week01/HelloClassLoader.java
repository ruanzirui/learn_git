package com.example.datastructor.jvm_01_01;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelloClassLoader extends ClassLoader{

    public static void main(String[] args) {

        try {
            Class<?> helloClass = new HelloClassLoader().findClass("Hello");
            Method helloMethod = helloClass.getMethod("hello");
            helloMethod.setAccessible(true);
            helloMethod.invoke(helloClass.newInstance());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        File file = new File("D:\\project\\dataStructor\\datastructorTest\\src\\main\\java\\com\\example\\datastructor\\jvm_01_01\\Hello.xlass");
        byte[] bytes = fileToBytes(file);

        return defineClass(name,bytes,0,bytes.length);
    }

    /**
     * 文件转为字节码数据组
     * @param file
     * @return
     */
    private byte[] fileToBytes(File file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            int read;
            while((read = inputStream.read()) != -1){
                out.write(255-read);
            }
            out.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();

    }

}
