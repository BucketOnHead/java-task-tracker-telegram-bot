package com.github.bucketonhead.service.sender;

import com.github.bucketonhead.utils.ResponseMessageUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageSender {
    void send(SendMessage msg);

    default void send(String text, Long chatId) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        send(msg);
    }

    default void sendError(String text, Long chatId) {
        text = ResponseMessageUtils.buildError(text);
        send(text, chatId);
    }

    default void sendParseMarkdown(String text, Long chatId) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(ParseMode.MARKDOWN)
                .build();
        send(msg);
    }
}
