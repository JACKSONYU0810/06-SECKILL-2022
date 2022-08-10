package com.jackson.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: AmqpConfig
 * Package: com.jackson.seckill.config
 * Description:
 *
 * @Date: 8/10/2022 3:29 PM
 * @Author: JacksonYu
 */
@Configuration
public class AmqpConfig {

    @Bean
    public Queue secKillQueue(){return new Queue("secKillQueue", true, false, false, null);}

    @Bean
    public DirectExchange secKillExchange(){return new DirectExchange("secKillExchange", true, false);}

    @Bean
    public Binding binding(Queue secKillQueue,DirectExchange secKillExchange){
        return BindingBuilder.bind(secKillQueue).to(secKillExchange).with("secKillRoutingKey");
    }
}
