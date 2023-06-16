package com.github.bucketonhead.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "bot")
@PropertySource("classpath:bot.properties")
@Getter
@Setter
public class BotConfig {
    private String name;
    private String token;
}
