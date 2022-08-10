package com.jackson.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jackson.seckill.common.Constants;
import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.mapper.GoodsMapper;
import com.jackson.seckill.model.Goods;
import com.jackson.seckill.service.GoodsService;
import com.netflix.discovery.converters.Auto;
import com.rabbitmq.client.Return;
import org.apache.http.Consts;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: GoodsServiceImpl
 * Package: com.jackson.seckill.service.impl
 * Description:
 *
 * @Date: 7/29/2022 12:14 PM
 * @Author: JacksonYu
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    @Override
    public List<Goods> queryGoodsList() {
        return goodsMapper.selectGoodsList();
    }

    @Override
    public Goods queryGoodsInfoById(Integer id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    //秒杀业务的主要逻辑
    @Override
    public ReturnObject secKill(Integer goodsId, String randomName, Integer uid) {

        ReturnObject returnObject = new ReturnObject();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        //1。根据统一库存前缀和randomName，从redis中获取库存，进行防止超卖操作(拦截一部分)
        String store = (String) redisTemplate.opsForValue().get(Constants.GOODS_STORE + randomName);
        if (store == null && "".equals(store)){
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("商品信息异常请确认后再购买");
            returnObject.setResult("");

            return returnObject;
        }

        //2。判断库存是否小于0，防止超卖（拦截一部分）
        if (Integer.valueOf(store)<0){
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("商品已被抢购完，请下次再来");
            returnObject.setResult("");

            return returnObject;
        }

        //3。根据统一限制前缀+randomName+uid，限制用户重复购买（拦截一部分）
        String purchaseLimit = (String) redisTemplate.opsForValue().get(Constants.PURCHASE_LIMIT+randomName+uid);
        if (purchaseLimit!=null && !"".equals(purchaseLimit)){

            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("您已购买过该商品,无法再次购买");
            returnObject.setResult("");

            return returnObject;
        }

        //4.限制用户访问总数量，使用redis的单线程机制
        Long currentLimining = redisTemplate.opsForValue().increment(Constants.CURRENT_LIMITING);
        if (currentLimining>1000){

            //一个线程自增一次，还要在自减一次，防止总量超过1000后，无高并发时，用户无法访问
            redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("服务繁忙，请稍后再试");
            returnObject.setResult("");

            return returnObject;
        }

        //将订单信息转化为json数据进行传输
        Map ordersMap = new HashMap();
        ordersMap.put("goodsId", goodsId);
        ordersMap.put("uid", uid);

        //5。使用redis的事务机制,来防止超卖和限购，实现减库存业务，同时监控库存key和限购key
        //返回一个list类型的result，根据list是否为空，判断事务是否执行成功
        Object result = redisTemplate.execute(new SessionCallback() {

            //RedisOperations是redisTemplate的父接口
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                //定义需要监视的key集合,监视库存和限购记录,防止超卖和限购
                List<String> watchKeyList = new ArrayList<>();
                watchKeyList.add(Constants.GOODS_STORE + randomName);
                watchKeyList.add(Constants.PURCHASE_LIMIT + randomName + uid);

                //执行监视,在监视期间,一旦有其他线程修改了监视的数据,则redis事务取消
                operations.watch(watchKeyList);

                //再次判断是否有库存和是否限购
                String store = (String) redisTemplate.opsForValue().get(Constants.GOODS_STORE + randomName);
                if (Integer.valueOf(store)<0){
                    operations.unwatch();
                    returnObject.setCode(Constants.ERROR);
                    returnObject.setMessage("商品已被抢购完，请下次再来");
                    returnObject.setResult("");

                    return returnObject;
                }

                //根据统一限制前缀+randomName+uid，限制用户重复购买（拦截一部分）
                String purchaseLimit = (String) redisTemplate.opsForValue().get(Constants.PURCHASE_LIMIT+randomName+uid);
                if (purchaseLimit!=null && !"".equals(purchaseLimit)){
                    //购买失败时，取消监控
                    operations.unwatch();
                    returnObject.setCode(Constants.ERROR);
                    returnObject.setMessage("您已购买过该商品,无法再次购买");
                    returnObject.setResult("");

                    return returnObject;
                }

                //执行到这，表示有库存，没限购，可以开始抢购
                //开启事务
                operations.multi();

                redisTemplate.opsForValue().decrement(Constants.GOODS_STORE + randomName);
                redisTemplate.opsForValue().set(Constants.PURCHASE_LIMIT+randomName+uid, "1");

                //将订单信息保存到redis中，防止订单信息未发送到队列中，在定时器中扫描掉单信息，重新发送到队列中
                redisTemplate.opsForValue().set(Constants.ORDER+goodsId+uid, JSONObject.toJSONString(ordersMap));


                //提交事务,返回一个list集合,集合size>0 success, size<0 failure
                //失败的原因可能是其他线程修改了key的数据,造成超卖和重复购买
                //因此另一个线程成功,这个线程就失败,确保了只有一个线程可以进行购买
                //100%防止了超卖和重复购买
                return operations.exec();
            }
        });

        //进入if，表示没有库存，或者限购
        if (result instanceof ReturnObject){
            //一个线程自增一次，还要在自减一次，防止总量超过1000后，无高并发时，用户无法访问
            redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);

            return (ReturnObject) result;
        }

        List list = (List) result;
        //进入if，表示抢购时，其他线程对监控的key进行修改，事务失败
        if (list.isEmpty()){
            //一个线程自增一次，还要在自减一次，防止总量超过1000后，无高并发时，用户无法访问
            redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);

            //事务失败后,递归调用秒杀方法,再次进行秒杀
            secKill(goodsId, randomName, uid);
        }

        /*//将订单信息转化为json数据进行传输
        Map ordersMap = new HashMap();
        ordersMap.put("goodsId", goodsId);
        ordersMap.put("uid", uid);*/

        //开始下单，避免直接操作数据库，应将订单消息发送到队列中
        amqpTemplate.convertAndSend("secKillExchange", "secKillRoutingKey", JSONObject.toJSONString(ordersMap));

        //一个线程自增一次，还要在自减一次，防止总量超过1000后，无高并发时，用户无法访问
        redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);
        returnObject.setCode(Constants.ERROR);
        returnObject.setMessage("抢购成功");
        returnObject.setResult("");

        return returnObject;
    }

    @Override
    public BigDecimal getGoodsPrice(Integer goodsId) {
        return goodsMapper.selectGoodsPriceByGoodsId(goodsId);
    }
}
