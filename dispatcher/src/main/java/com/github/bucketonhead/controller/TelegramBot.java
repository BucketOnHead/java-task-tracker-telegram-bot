package com.github.bucketonhead.controller;

import com.github.bucketonhead.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UpdateController updateController;

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
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
