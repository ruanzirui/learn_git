package com.example.datastructor.jvm_week02_netty;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class OkHttpClientNio {

    private static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
//        String body1 = getBody1(client, "http://localhost:8801");
//        String body1 = getBody1(client, "https://www.baidu.com");
        httpGet(client,"http://localhost:8888");
//        System.out.println("body1: "+body1);
        client = null;
    }


    private static String getBody1(OkHttpClient client, String url){

        Request request = new Request.Builder()
                .get()
                .url(url)
                .addHeader("Connection", "close")
                .build();
        String responseData = null;
        try {
            Response response = client.newCall(request).execute();
            responseData = response.body().string();
            System.out.println(responseData);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            client = null;
        }
        return responseData;

    }

    private static void httpGet(OkHttpClient client, String url){
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        Call call = client.newCall(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    System.out.println("run: " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
