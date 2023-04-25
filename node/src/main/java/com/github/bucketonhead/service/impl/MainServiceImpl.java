package com.github.bucketonhead.service.impl;

import com.github.bucketonhead.dao.RawDataDAO;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.service.MainService;
import com.github.bucketonhead.service.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var message = update.getMessage();
        var botMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text("Сообщение получено! 🙂")
                .build();
        producerService.producerAnswer(botMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
