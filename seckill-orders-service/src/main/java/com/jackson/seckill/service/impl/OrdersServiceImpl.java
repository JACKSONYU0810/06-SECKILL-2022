package com.jackson.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jackson.seckill.common.Constants;
import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.mapper.OrdersMapper;
import com.jackson.seckill.model.Orders;
import com.jackson.seckill.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * ClassName: OrdersServiceImpl
 * Package: com.jackson.seckill.service.impl
 * Description:
 *
 * @Date: 8/10/2022 5:11 PM
 * @Author: JacksonYu
 */
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${seckill.goodsService}")
    private String goodsService;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    @Override
    public int order(Orders orders) {
        int result = 0;
        try {
            String url = goodsService + "getGoodsPrice/" + orders.getGoodsId();
            Map goodsMap = restTemplate.getForObject(url, Map.class);

            BigDecimal goodsPrice = new BigDecimal((Double) goodsMap.get("result"));
            orders.setBuyNum(1);
            orders.setCreateTime(new Date());
            orders.setStatus(1);
            orders.setBuyPrice(goodsPrice);
            orders.setOrderMoney(goodsPrice.multiply(new BigDecimal(orders.getBuyNum())));

            //添加订单到数据库中
            result = ordersMapper.insertSelective(orders);

            //将下单成功的信息，保存到redis中
            redisTemplate.setKeySerializer(stringRedisSerializer);
            redisTemplate.setValueSerializer(stringRedisSerializer);

            //订单查询成功后，将redis中备份的订单信息删除
            redisTemplate.delete(Constants.ORDER + orders.getGoodsId() + orders.getUid());

            redisTemplate.opsForValue().set(Constants.ORDER_RESULT + orders.getGoodsId() + orders.getUid(), JSONObject.toJSONString(orders));

        } catch (Exception e) {
            String message = e.getMessage();
            if (message.indexOf("for key 'idx_uid_goodsid'") >= 0) {
                redisTemplate.setKeySerializer(stringRedisSerializer);
                redisTemplate.setValueSerializer(stringRedisSerializer);
                System.out.println("向数据库中重复插入数据,违反了唯一约束");

                //程序到这,因为唯一约束冲突那么我们需要清除Redis中的那个订单的备份数据
                redisTemplate.delete(Constants.ORDER + orders.getGoodsId() + orders.getUid());

                return 2;
            }
            return 3;
        }
        return result;
    }

    @Override
    public ReturnObject getOrderResult(Integer goodsId, Integer uid) {
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        String orderStr = (String) redisTemplate.opsForValue().get(Constants.ORDER_RESULT+goodsId+uid);
        ReturnObject returnObject = new ReturnObject();
        if (orderStr==null || "".equals(orderStr)){
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("未获取到订单信息");
            returnObject.setResult("");
            return returnObject;
        }

        returnObject.setCode(Constants.OK);
        returnObject.setMessage("获取订单成功");
        returnObject.setResult(JSONObject.parseObject(orderStr,Orders.class));

        return returnObject;
    }
}
