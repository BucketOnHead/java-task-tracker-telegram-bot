package com.github.bucketonhead.service.rabbitmq;

import com.github.bucketonhead.constants.RabbitQueue;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void produce(String rabbitQueue, Update update);

    default void produceTextMessage(Update update) {
        produce(RabbitQueue.TEXT_MESSAGE_UPDATE, update);
    }
}
