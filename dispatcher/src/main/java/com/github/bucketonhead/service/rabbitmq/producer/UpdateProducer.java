package com.github.bucketonhead.service.rabbitmq.producer;

import com.github.bucketonhead.consts.RabbitQueue;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void produce(String rabbitQueue, Update update);

    default void produceTextMessage(Update update) {
        produce(RabbitQueue.TEXT_MESSAGE_UPDATE, update);
    }
}
