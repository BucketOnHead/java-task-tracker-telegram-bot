package com.github.bucketonhead.service.impl;

import com.github.bucketonhead.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateProducerImpl implements UpdateProducer {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitQueue, Update update) {
        var message = update.getMessage();
        log.debug("Сообщение[text='{}'] от пользователя[id={}] добавлено в очередь[name='{}']",
                message.getText(), message.getFrom().getId(), rabbitQueue);
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }
}
