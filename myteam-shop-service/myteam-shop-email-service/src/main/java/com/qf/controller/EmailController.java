package com.qf.controller;

import dto.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${activeServerUrl}")
    private String activeServerUrl;

    @RequestMapping("/send/{addr}/{uuid}")
    public ResultBean sendEmail(@PathVariable String addr,
                                @PathVariable String uuid){

        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper mailMessage = new MimeMessageHelper(message, true);
            mailMessage.setSubject("请激活您在本中心的账号");

            //读取模板内容
            Context context = new Context();
            context.setVariable("username",addr.substring(0,addr.lastIndexOf("@")));
            context.setVariable("url",activeServerUrl+uuid);

            String info = templateEngine.process("emailTemplate", context);

            mailMessage.setText(info,true);

            mailMessage.setFrom("842749680@qq.com");
            mailMessage.setTo(addr);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return ResultBean.success("email send success");
    }
}
