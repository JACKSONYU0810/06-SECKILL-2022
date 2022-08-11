package com.jackson.seckill.controller;

import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.model.Orders;
import com.jackson.seckill.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: OrderController
 * Package: com.jackson.seckill.controller
 * Description:
 *
 * @Date: 8/11/2022 3:26 PM
 * @Author: JacksonYu
 */
@RestController
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @RequestMapping("/getOrderResult/{goodsId}/{uid}")
    public Object getOrderResult(@PathVariable Integer goodsId,@PathVariable Integer uid){


        ReturnObject returnObject = ordersService.getOrderResult(goodsId,uid);

        return returnObject;
    }
}
