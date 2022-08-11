package com.jackson.seckill.controller;

import com.jackson.seckill.common.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * ClassName: OrderController
 * Package: com.jackson.seckill.controller
 * Description:
 *
 * @Date: 8/11/2022 3:26 PM
 * @Author: JacksonYu
 */
@Controller
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${seckill.ordersService}")
    private String ordersServicePath;

    @RequestMapping("/getOrderResult/{goodsId}")
    public @ResponseBody Object getOrderResult(@PathVariable Integer goodsId){

        Integer uid = 1;
        String url = ordersServicePath + "getOrderResult/"+goodsId+"/"+uid;
        ReturnObject returnObject = restTemplate.getForObject(url, ReturnObject.class);

        return returnObject;
    }
}
