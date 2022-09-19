package com.wby.community;

import com.wby.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//启用CommunityApplication作为配置类
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博，可以嫖娼，可以吸毒，nmsl，哈哈哈！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以赌☆博，可以嫖◇娼，可以吸♧毒，nm※sl，哈哈哈！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
