package com.github.bucketonhead.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@PropertySource("classpath:bot.properties")
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var message = update.getMessage();
        log.debug("Получено сообщение от пользователя: {}", message.getText());

        var botMessage = new SendMessage();
        botMessage.setChatId(message.getChatId());
        botMessage.setText(String.format("Получено сообщение: %s", message.getText()));

        sendBotMessageIgnoreException(botMessage);
    }

    public void sendBotMessageIgnoreException(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Message was not sent: {}", e.getMessage());
            }
        }
    }
}
