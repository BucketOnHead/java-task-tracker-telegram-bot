package com.github.bucketonhead.service.rabbitmq.consumer;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ConsumerService {
    void consumeTextMessageUpdate(Update update);
}
