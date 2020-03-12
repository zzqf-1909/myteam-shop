package com.qf.controller;

import constant.RedisConstant;
import dto.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.RedisUtil;
import vo.CartItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 1. 当前用户没有购物车
     *    新建购物车，把商品添加到购物车中，再把商品放到redis中
     * 2. 当前用户有购物车但是没有商品
     *    先从redis中获得购物车，再把商品添加进去，在放入redis中
     * 3. 当前用户有购物车也有商品
     *    先从redis中拿到购物车，再获取商品数量，再把商品数量合并更新购物车，再放入redis
     * @param uuid
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/addProduct/{uuid}/{productId}/{count}")
    public ResultBean addProduct(@PathVariable String uuid,
                                 @PathVariable Integer productId,
                                 @PathVariable Integer count){

        //user:cart1:uuid
        String redisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, uuid);

        //获得redis中的购物车
        Object o = redisTemplate.opsForValue().get(redisKey);

        //1. 当前用户没有购物车
        if(o==null){

            //封装购物车对象
            CartItem cartItem = new CartItem();
            cartItem.setProductId(productId);
            cartItem.setCount(count);
            cartItem.setUpdateTime(new Date());

            //存入购物车中
            List<CartItem> carts =  new ArrayList<>();
            carts.add(cartItem);
            //存到redis中
            redisTemplate.opsForValue().set(redisKey,carts);
            return ResultBean.success(carts,"添加购物车成功");
        }

        //第2、3中情况
        List<CartItem> carts = (List<CartItem>) o;

        for (CartItem cartItem : carts) {
            if (cartItem.getProductId().intValue() == productId.intValue()) {
                //当前用户有购物车，且购物车中有该商品
                cartItem.setCount(cartItem.getCount()+count);
               //更新商品时间
                cartItem.setUpdateTime(new Date());
                redisTemplate.opsForValue().set(redisKey,carts);
                return ResultBean.success(carts,"添加购物车成功");
            }
        }

        //当前用户有购物车但是没有商品
        CartItem cartItem = new CartItem();
        cartItem.setProductId(productId);
        cartItem.setCount(count);
        cartItem.setUpdateTime(new Date());
        carts.add(cartItem);
        //存到redis中
        redisTemplate.opsForValue().set(redisKey,carts);
        return ResultBean.success(carts,"添加购物车成功");
    }
}
