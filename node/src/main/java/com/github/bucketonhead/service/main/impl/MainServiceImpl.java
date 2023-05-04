package com.github.bucketonhead.service.main.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.dao.RawDataJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.main.MainService;
import com.github.bucketonhead.service.main.enums.ServiceCommand;
import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.utils.TextMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final RawDataJpaRepository rawDataJpaRepository;
    private final AppUserJpaRepository appUserJpaRepository;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var msg = update.getMessage();
        var appUser = findOrSaveAppUser(msg.getFrom());

        var serviceCommand = ServiceCommand.fromValue(msg.getText());
        if (ServiceCommand.CANCEL.equals(serviceCommand)) {
            processCancelCommand(appUser, msg);
            return;
        }

        if (BotState.BASIC_STATE == appUser.getState()) {
            processBasicStateCommand(appUser, msg);
        } else if (BotState.WAIT_FOR_EMAIL_STATE == appUser.getState()) {
            // TODO: реализовать после добавления email-сервиса
        } else {
            log.error("state: {}, не реализован", appUser.getState());
            var text = "Разработчик допустил ошибку при реализации " +
                    "этой функциональности, попробуйте позже! " +
                    "А пока вернём вас в главное меню ☺";
            var responseMessage = TextMessageUtils.buildErrorMessage(text);
            sendResponseMessage(responseMessage, msg.getChatId());
            processCancelCommand(appUser, msg);
        }
    }

    private void processBasicStateCommand(AppUser appUser, Message msg) {
        String responseMessage;
        if (!msg.getText().startsWith(ServiceCommand.PREFIX)) {
            responseMessage = "Я бы с удовольствие поговорил, " +
                    "но я просто бот, выполняющий команды ☺";
            sendResponseMessage(responseMessage, msg.getChatId());
            return;
        }

        var serviceCommand = ServiceCommand.fromValue(msg.getText());
        if (serviceCommand == null) {
            var text = "Команда не распознана!";
            responseMessage = TextMessageUtils.buildErrorMessage(text);
        } else if (ServiceCommand.HELP == serviceCommand) {
            var text = "Список доступных команд:%n%n" +
                    "%s - отмена выполнения текущей команды";
            responseMessage = String.format(text, ServiceCommand.CANCEL);
        } else if (ServiceCommand.START == serviceCommand) {
            var text = "Приветствую! Чтобы посмотреть список " +
                    "доступных команд используйте %s";
            responseMessage = String.format(text, ServiceCommand.HELP);
        } else {
            var text = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже!";
            responseMessage = TextMessageUtils.buildErrorMessage(text);
        }

        sendResponseMessage(responseMessage, msg.getChatId());
    }

    private void processCancelCommand(AppUser appUser, Message msg) {
        String responseMessage;
        if (BotState.BASIC_STATE == appUser.getState()) {
            responseMessage = "Вы уже в главном меню 😉";
        } else {
            appUser.setState(BotState.BASIC_STATE);
            appUserJpaRepository.save(appUser);
            responseMessage = "Вернули вас в главное меню!";
        }
        sendResponseMessage(responseMessage, msg.getChatId());
    }

    private void sendResponseMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        producerService.producerAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataJpaRepository.save(rawData);
    }

    private AppUser findOrSaveAppUser(User tgUser) {
        AppUser persistenceAppUser = appUserJpaRepository.findByTelegramUserId(tgUser.getId());
        if (persistenceAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(tgUser.getId())
                    .firstName(tgUser.getFirstName())
                    .lastName(tgUser.getLastName())
                    .username(tgUser.getUserName())
                    // TODO: поменять на false после реализации email-сервиса
                    .isActive(Boolean.TRUE)
                    .state(BotState.BASIC_STATE)
                    .build();
            persistenceAppUser = appUserJpaRepository.save(transientAppUser);
        }
        return persistenceAppUser;
    }
}
