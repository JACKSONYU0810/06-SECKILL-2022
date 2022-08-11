package com.jackson.seckill.service;

import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.model.Goods;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: GoodsService
 * Package: com.jackson.seckill.service
 * Description:
 *
 * @Date: 7/29/2022 12:14 PM
 * @Author: JacksonYu
 */
public interface GoodsService {

    List<Goods> queryGoodsList();

    Goods queryGoodsInfoById(Integer id);

    ReturnObject secKill(Integer goodsId, String randomName, Integer uid);

    BigDecimal getGoodsPrice(Integer goodsId);
}
