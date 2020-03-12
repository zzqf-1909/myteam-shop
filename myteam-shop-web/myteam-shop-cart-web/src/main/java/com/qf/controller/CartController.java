package com.qf.controller;

import com.qf.entity.TUser;
import constant.CookieConstant;
import dto.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.UUID;

@Controller
@RequestMapping("cart")
public class CartController {


    @Autowired
    private RestTemplate restTemplate;

    /**
     * 1. 当前用户没有购物车
     *    新建购物车，把商品添加到购物车中，再把商品放到redis中
     * 2. 当前用户有购物车但是没有商品
     *    先从redis中获得购物车，再把商品添加进去，在放入redis中
     * 3. 当前用户有购物车也有商品
     *    先从redis中拿到购物车，再获取商品数量，再把商品数量合并更新购物车，再放入redis
     * @param uuid 浏览器拿到的uuid
     * @param productId 产品的id
     * @param count 产品数量
     * @return
     */
    @RequestMapping("add/{productId}/{count}")
    @ResponseBody
    public ResultBean addProduct(@CookieValue(name = CookieConstant.USER_CART_1, required = false) String uuid,
                                 @PathVariable Integer productId,
                                 @PathVariable int count,
                                 HttpServletRequest request,
                                 HttpServletResponse response){

        Object u = request.getAttribute("user");
        //=================已登录状态下的购物车===================
//        if(u!=null){
//            TUser user = (TUser) u;
//            Integer userId = user.getId();
//
//        }


        //=================未登录状态下的购物车===================
        //把该商品添加到购物车，把购物车放到redis中
        if(uuid==null||"".equals(uuid)){
            //没有购物车的情况下，新建一个uuid当做购物车的编号
            //uuid为空的话再生成一个uuid放到cookie里给客户端
            uuid = UUID.randomUUID().toString();
            //返回uuid给cookie
            Cookie cookie = new Cookie(CookieConstant.USER_CART_1, uuid);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        String url = String.format("http://cart-service/cart/addProduct/%s/%s/%s",uuid,productId,count);
        ResultBean resultBean = restTemplate.getForObject(url, ResultBean.class);
        return resultBean;
    }
}
