package com.jackson.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.jackson.seckill.model.Orders;
import com.jackson.seckill.service.OrdersService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: RabbitMQListener
 * Package: com.jackson.seckill.listener
 * Description:
 *
 * @Date: 8/10/2022 5:03 PM
 * @Author: JacksonYu
 */
@Component
public class RabbitMQListener {

    @Autowired
    private OrdersService ordersService;

    @RabbitListener(queues = {"secKillQueue"})
    private void rabbitListener(String message){
        Orders orders = JSONObject.parseObject(message, Orders.class);

        int result = ordersService.order(orders);
    }
}
