package com.jackson.seckill.timer;

import com.jackson.seckill.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * ClassName: MySchedule
 * Package: com.jackson.seckill.timer
 * Description:
 *
 * @Date: 8/8/2022 4:25 PM
 * @Author: JacksonYu
 */
@Component
public class MySchedule {

    @Value("${seckill.goodsService}")
    private String goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    //实际开发中,不会这样设定,而是在秒杀开始之前或每天23:55时执行一次
    @Scheduled(cron = "0/30 * * * * *")
    public void initSecKillGoodsToRedis(){
        String url = goodsService + "goodsList/";
        Map goodsList = restTemplate.getForObject(url, Map.class);

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        List<Map> list = (List<Map>) goodsList.get("result");
        for (Map map : list) {
            redisTemplate.opsForValue().setIfAbsent(Constants.GOODS_STORE+map.get("randomName"), map.get("store")+"");
        }
    }

}
