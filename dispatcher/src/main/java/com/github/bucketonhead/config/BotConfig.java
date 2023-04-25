package com.github.bucketonhead.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:bot.properties")
@Getter
public class BotConfig {
    private final String name;
    private final String token;

    public BotConfig(@Value("${bot.name}") String name,
                     @Value("${bot.token}") String token) {
        this.name = name;
        this.token = token;
    }
}
