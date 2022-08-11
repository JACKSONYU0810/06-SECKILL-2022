package com.jackson.seckill.mapper;

import com.jackson.seckill.model.Goods;

import java.math.BigDecimal;
import java.util.List;
public interface GoodsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Goods record);

    int insertSelective(Goods record);

    Goods selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Goods record);

    int updateByPrimaryKey(Goods record);

    List<Goods> selectGoodsList();

    BigDecimal selectGoodsPriceByGoodsId(Integer goodsId);
}