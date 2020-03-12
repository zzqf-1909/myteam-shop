package com.qf.controller;

import com.qf.service.RegisterService;
import constant.RedisConstant;
import dto.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import util.RedisUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/email/{addr}/{password}")
    public ResultBean regisByEmail(@PathVariable String addr,
                                   @PathVariable String password){
//        1.生成uuid
        String uuid = UUID.randomUUID().toString();
//        2.发邮件
        String url = String.format("http://email-service/email/send/%s/%s",addr,uuid);
        ResultBean resultBean = restTemplate.getForObject(url, ResultBean.class);
        ResultBean resultBean1 = null;
        if(resultBean.getErrno()==0) {
//        3.url组织好的键值对存到redis
            redisTemplate.opsForValue().set(RedisUtil.getRedisKey(
                    RedisConstant.REGISTER_EMAIL_URL_KEY_PRE, uuid), addr, 15, TimeUnit.MINUTES);
//        4.去数据库中创建该用户
            resultBean1 = restTemplate.getForObject(String.format("http://email-service/%s/%s",addr,password),ResultBean.class);
        }
        return resultBean1;
    }
}
