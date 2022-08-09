package com.jackson.seckill.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ClassName: RestTemplateConfig
 * Package: com.jackson.seckill.config
 * Description:
 *
 * @Date: 8/8/2022 5:04 PM
 * @Author: JacksonYu
 */
@Configuration
public class RestTemplateConfig {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
