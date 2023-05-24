package com.github.bucketonhead.service.rabbitmq.impl;

import com.github.bucketonhead.constants.RabbitQueue;
import com.github.bucketonhead.service.main.MainService;
import com.github.bucketonhead.service.rabbitmq.ConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;

    @Override
    @RabbitListener(queues = RabbitQueue.TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
        log.debug("Получен update от пользователя[id={}] из очереди {}",
                update.getMessage().getFrom().getId(), RabbitQueue.TEXT_MESSAGE_UPDATE);
        try {
            mainService.processTextMessage(update);
        } catch (RuntimeException ex) {
            log.error("main service error: {}", ex.getMessage());
        }
    }
}
