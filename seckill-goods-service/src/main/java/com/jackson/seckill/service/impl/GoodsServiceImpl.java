package com.jackson.seckill.service.impl;

import com.jackson.seckill.common.ReturnObject;
import com.jackson.seckill.mapper.GoodsMapper;
import com.jackson.seckill.model.Goods;
import com.jackson.seckill.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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



        return null;
    }
}
