package com.jackson.seckill.controller;

import com.jackson.seckill.common.Constants;
import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.model.Goods;
import com.jackson.seckill.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: GoodsController
 * Package: com.jackson.seckill.controller
 * Description:
 *
 * @Date: 7/29/2022 11:20 AM
 * @Author: JacksonYu
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @RequestMapping("/goodsList")
    public Object goodsList(){

        List<Goods> goodsList = goodsService.queryGoodsList();

        ReturnObject returnObject = new ReturnObject();
        returnObject.setCode(Constants.OK);
        returnObject.setMessage("操作成功");
        returnObject.setResult(goodsList);

        return returnObject;
    }

    @RequestMapping("/goodsInfo/{id}")
    public Object goodsList(@PathVariable Integer id){

        Goods goodsInfo = goodsService.queryGoodsInfoById(id);

        ReturnObject returnObject = new ReturnObject();
        returnObject.setCode(Constants.OK);
        returnObject.setMessage("操作成功");
        returnObject.setResult(goodsInfo);

        return returnObject;
    }

    @RequestMapping("/secKill/{goodsId}/{randomName}/{uid}")
    @ResponseBody
    public Object secKill(@PathVariable Integer goodsId,@PathVariable String randomName,@PathVariable Integer uid){

        ReturnObject returnObject = goodsService.secKill(goodsId,randomName,uid);

        return returnObject;
    }


    @RequestMapping("/getGoodsPrice/{goodsId}")
    public Object getGoodsPrice(@PathVariable Integer goodsId){

        BigDecimal goodsPrice = goodsService.getGoodsPrice(goodsId);

        ReturnObject returnObject = new ReturnObject();
        returnObject.setCode(Constants.OK);
        returnObject.setMessage("获取商品价格成功");
        returnObject.setResult(goodsPrice);

        return returnObject;
    }

}
