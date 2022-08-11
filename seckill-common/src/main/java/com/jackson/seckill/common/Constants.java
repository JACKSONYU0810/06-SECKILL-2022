package com.jackson.seckill.common;

/**
 * ClassName: Constants
 * Package: com.jackson.seckill.common
 * Description:
 *
 * @Date: 7/29/2022 12:31 PM
 * @Author: JacksonYu
 */
public final class Constants {

    /**
     * 定义商品库存前缀
     */
    public static final String GOODS_STORE = "GOODS_STORE";

    /**
     * 定义用户购买限制
     */
    public static final String PURCHASE_LIMIT = "PURCHASE_LIMIT:";

    /**
     * 定义用户总访问量
     */
    public static final String CURRENT_LIMITING = "CURRENT_LIMITING";

    /**
     * 定义备份订单统一前缀
     */
    public static final String ORDER = "ORDER";

    /**
     * 定义订单成功前缀
     */
    public static final String ORDER_RESULT = "ORDER_RESULT";

    /**
     * 私有构造方法，不能被实例化
     */
    private Constants(){};

    /**
     * 操作成功
     */
    public static final String OK = "0";

    /**
     * 操作错误
     */
    public static final String ERROR = "1";

}
