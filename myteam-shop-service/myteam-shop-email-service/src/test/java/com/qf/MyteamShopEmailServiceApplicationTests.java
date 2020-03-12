package com.qf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class MyteamShopEmailServiceApplicationTests {

    @Autowired
    private JavaMailSender sender;

    @Test
    void contextLoads() {

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("测试邮件主题-");
        mailMessage.setText("测试邮件内容");
        mailMessage.setFrom("842749680@qq.com");
        mailMessage.setTo("zzqf1909@163.com");
        sender.send(mailMessage);


    }

}
