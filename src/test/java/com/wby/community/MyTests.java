package com.wby.community;


import java.util.Date;

public class MyTests {

    public static void main(String[] args) {
        long seconds = 60 * 60 * 24 * 30;
        Date date = new Date(System.currentTimeMillis() + seconds * 1000);
        System.out.println(date);
    }
}
