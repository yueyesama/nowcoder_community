package com.wby.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "E:/Programs/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.baidu.com d:/work/data/wk-images/4.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("OK.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
