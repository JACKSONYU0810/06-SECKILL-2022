package com.jackson.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jackson.seckill.common.Constants;
import com.jackson.seckill.mapper.OrdersMapper;
import com.jackson.seckill.model.Orders;
import com.jackson.seckill.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
            String url = goodsService+"getGoodsPrice/"+orders.getGoodsId();
            Map goodsMap = restTemplate.getForObject(url, Map.class);

            BigDecimal goodsPrice = (BigDecimal) (goodsMap.get("result"));
            orders.setBuyNum(1);
            orders.setCreateTime(new Date());
            orders.setStatus(1);
            orders.setBuyPrice(goodsPrice);
            orders.setOrderMoney(goodsPrice.multiply(new BigDecimal(orders.getBuyNum())));

            //添加订单到数据库中
            result = ordersMapper.insertSelective(orders);

            //订单查询成功后，将redis中备份的订单信息删除
            redisTemplate.delete(Constants.ORDER+orders.getGoodsId()+orders.getUid());

            //将下单成功的信息，保存到redis中
            redisTemplate.setKeySerializer(stringRedisSerializer);
            redisTemplate.setValueSerializer(stringRedisSerializer);

            redisTemplate.opsForValue().set(Constants.ORDER_RESULT+orders.getGoodsId()+orders.getUid(), JSONObject.toJSONString(orders));

        } catch (RestClientException e) {
            e.printStackTrace();
        }


        return 0;
    }
}
