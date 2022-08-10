package com.jackson.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@MapperScan("com.jackson.seckill.mapper")
@EnableEurekaClient
public class SeckillOrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillOrdersServiceApplication.class, args);
    }

}
