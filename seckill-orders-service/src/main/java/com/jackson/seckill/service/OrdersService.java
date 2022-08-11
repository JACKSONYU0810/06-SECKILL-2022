package com.jackson.seckill.service;

import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.model.Orders;

/**
 * ClassName: OrdersService
 * Package: com.jackson.seckill.service
 * Description:
 *
 * @Date: 8/10/2022 5:02 PM
 * @Author: JacksonYu
 */
public interface OrdersService {


    int order(Orders orders);

    ReturnObject getOrderResult(Integer goodsId, Integer uid);
}
