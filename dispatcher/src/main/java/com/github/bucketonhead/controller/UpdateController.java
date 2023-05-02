package com.github.bucketonhead.controller;

import com.github.bucketonhead.service.UpdateProducer;
import com.github.bucketonhead.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class UpdateController {
    private final UpdateProducer updateProducer;
    private final TelegramBot telegramBot;

    public UpdateController(@Lazy TelegramBot telegramBot,
                            UpdateProducer updateProducer) {
        this.telegramBot = telegramBot;
        this.updateProducer = updateProducer;
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendBotMessageIgnoreException(sendMessage);
        log.debug("Отправлено сообщение[text='{}'] от бота в чат[id={}]",
                sendMessage.getText(), sendMessage.getChatId());
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }

        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Unsupported message type is received: " + update);
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.hasText()) {
            log.debug("Получено сообщение[text='{}'] от пользователя[id={}]",
                    message.getText(), message.getFrom().getId());
            processTextMessage(update);
        } else {
            log.error("Получен не поддерживаемый тип сообщения от пользователя[id={}]",
                    message.getFrom().getId());
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processTextMessage(Update update) {
        updateProducer.produceTextMessage(update);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        String text = "⚠  Ошибка\n\nПолучен не поддерживаемый тип сообщения!";
        setView(MessageUtils.buildTextMessage(update, text));
    }
}
