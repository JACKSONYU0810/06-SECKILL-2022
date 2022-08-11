package com.jackson.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jackson.seckill.mapper")
public class SeckillGoodsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillGoodsServiceApplication.class, args);
    }

}
