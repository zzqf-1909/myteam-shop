package com.qf.controller;

import com.qf.entity.TProduct;
import com.qf.mapper.TProductMapper;
import constant.RedisConstant;
import dto.ResultBean;
import dto.TProductCartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import util.RedisUtil;
import vo.CartItem;

import java.util.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TProductMapper tProductMapper;

    /**
     * 1. 当前用户没有购物车
     *    新建购物车，把商品添加到购物车中，再把商品放到redis中
     * 2. 当前用户有购物车但是没有商品
     *    先从redis中获得购物车，再把商品添加进去，在放入redis中
     * 3. 当前用户有购物车也有商品
     *    先从redis中拿到购物车，再获取商品数量，再把商品数量合并更新购物车，再放入redis
     * @param id
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/addProduct/{id}/{productId}/{count}")
    public ResultBean addProduct(@PathVariable String id,
                                 @PathVariable Integer productId,
                                 @PathVariable Integer count){
        System.out.println(id);

        //user:cart1:uuid
        String redisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, id);

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

    //清空购物车
    @RequestMapping("/clean/{id}")
    public ResultBean clean(@PathVariable String id){
        String redisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, id);
        redisTemplate.delete(redisKey);

        return ResultBean.success("清除购物车成功");
    }

    //更新购物车
    @RequestMapping("/update/{id}/{productId}/{count}")
    public ResultBean update(@PathVariable String id,
                             @PathVariable Integer productId,
                             @PathVariable Integer count){

        if (id!=null&&!"".equals(id)) {
            //组织redis的键
            String redisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, id);
            //获得键中的值
            Object o = redisTemplate.opsForValue().get(redisKey);
            //如果存在
            if(o!=null){
                List<CartItem> cartItems = (List<CartItem>) o;
                for(CartItem cartItem : cartItems){
                    if (cartItem.getProductId().intValue() == productId.intValue()) {
                        cartItem.setCount(count);
                        cartItem.setUpdateTime(new Date());
                        redisTemplate.opsForValue().set(redisKey,cartItems);
                        return ResultBean.success(cartItems,"购物车更新成功");
                    }
                }
            }
        }

        return ResultBean.error("当前用户没有购物车");
    }

    //展示购物车
    @RequestMapping("/show/{id}")
    public ResultBean show(@PathVariable String id){//user:cart1:userId

        if (id!=null&&!"".equals(id)) {
            String redisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, id);
            Object o = redisTemplate.opsForValue().get(redisKey);
            if (o!=null) {
                List<CartItem> cartItems = (List<CartItem>) o;
                List<TProductCartDTO> products = new ArrayList<>();
                for (CartItem cartItem : cartItems) {
                    String productKey = RedisUtil.getRedisKey(RedisConstant.PRODUCT_PRE, cartItem.getProductId().toString());
                    //先取出商品对应的键值对
                    TProduct pro = (TProduct) redisTemplate.opsForValue().get(productKey);
                    if (pro == null) {
                        //去数据库那对应的数据，再存入redis
                        pro = tProductMapper.selectByProductId(cartItem.getProductId());
                        redisTemplate.opsForValue().set(productKey,pro);
                    }
                    //pro存在
                    TProductCartDTO tProductCartDTO = new TProductCartDTO();

                    //封装
                    tProductCartDTO.setTProduct(pro);
                    tProductCartDTO.setCount(cartItem.getCount());
                    tProductCartDTO.setUpdateTime(cartItem.getUpdateTime());

                    //村如 product 集合
                    products.add(tProductCartDTO);
                }

                //对集合中的元素进行排序，comparator用来指明排序依据
                Collections.sort(products, new Comparator<TProductCartDTO>() {
                    @Override
                    public int compare(TProductCartDTO o1, TProductCartDTO o2) {
                        return (int) (o2.getUpdateTime().getTime()-o1.getUpdateTime().getTime());
                    }
                });

                return ResultBean.success(products);
            }
        }

        return ResultBean.success("当前用户没有购物车");
    }

    //合并购物车
    /*
    1.未登录状态下没有购物车，不需要合并 ==> 合并成功
    2.未登录状态下有购物车，但已登录没有购物车 ==> 把未登录变成已登录
    3.未登录状态下有购物车，但已登录状态下也有购物车，而且购物车中的商品有重复==》难点！
     */
    @RequestMapping("/merge/{userId}/{uuid}")
    public ResultBean merge(@PathVariable String userId,
                            @PathVariable String uuid){

        //先获得两个购物车
        String loginRedisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, userId);
        String noLoginRedisKey = RedisUtil.getRedisKey(RedisConstant.USER_CART1_PRE, uuid);
        //已登录购物车
        Object loginCart = redisTemplate.opsForValue().get(loginRedisKey);
        //未登录购物车
        Object noLoginCart = redisTemplate.opsForValue().get(noLoginRedisKey);

        //情况1：
        if (noLoginCart==null) {
            return ResultBean.success("未登录状态下没有购物车，不需要合并");
        }

        //情况2：
        if (loginCart == null) {
            //未登录变成已登录
            redisTemplate.opsForValue().set(loginRedisKey,noLoginCart);
            //把未登录清空
            redisTemplate.delete(noLoginRedisKey);
            //因为放到未已登录的购物车的之后,没有从redis中获取,只有未登录购物车
            return ResultBean.success(noLoginCart,"合并成功");
        }

        //情况3:
        //创建两种状态下的集合
        List<CartItem> loginCarts = (List<CartItem>) loginCart;
        List<CartItem> nologinCarts = (List<CartItem>) noLoginCart;

        HashMap<Integer, CartItem> map = new HashMap<>();
        //先将未登录购物车存入map
        for (CartItem noLoginCartItem : nologinCarts) {
            map.put(noLoginCartItem.getProductId(),noLoginCartItem);
        }
        //存入已登录的商品
        for (CartItem loginCartItem : loginCarts) {
            //匹配添加到map中的商品是否存
            CartItem existProduct = map.get(loginCartItem.getProductId());
            if (existProduct != null ) {
                //已存在，更新个数
                existProduct.setCount(existProduct.getCount()+loginCartItem.getCount());
            }
            if (existProduct == null) {
                //不存在，直接放进去
                map.put(loginCartItem.getProductId(),loginCartItem);
            }
        }

        //合并后删除原来未登录的购物车
        redisTemplate.delete(noLoginRedisKey);
        //把新的购物车存入redis中
        Collection<CartItem> values = map.values();
        List<CartItem> newCart = new ArrayList<>(values);
        redisTemplate.opsForValue().set(loginRedisKey,newCart);
        return ResultBean.success(newCart,"合并成功");
    }
}
