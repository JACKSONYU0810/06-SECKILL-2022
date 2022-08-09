package com.jackson.seckill.common;

import lombok.Data;

/**
 * ClassName: ReturnObject
 * Package: com.jackson.seckill.common
 * Description:
 *
 * @Date: 7/29/2022 12:21 PM
 * @Author: JacksonYu
 */
@Data
public final class ReturnObject {

    /**
     *返回数据的响应编码，0 表示成功，1 表示失败
     */
    private String code;

    /**
     * 返回的响应信息，如操作成功，操作失败
     */
    private String message;

    /**
     *返回的数据封装对象
     */
    private Object result;

}
