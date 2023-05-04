package com.github.bucketonhead.service.main.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.dao.RawDataJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.main.MainService;
import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.service.main.enums.ServiceCommand;
import com.github.bucketonhead.utils.TextMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
        var appUser = findOrSaveAppUser(update.getMessage().getFrom());
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);
        if (ServiceCommand.CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BotState.BASIC_STATE.equals(appUser.getState())) {
            output = processBasicStateCommand(appUser, text);
        } else if (BotState.WAIT_FOR_EMAIL_STATE.equals(appUser.getState())) {
            // TODO: реализовать после добавления email-сервиса
        } else {
            log.error("Unknown user state: " + appUser.getState());
            String botMessage = "Разработчик допустил ошибку при реализации " +
                    "этой функциональности, попробуйте позже! Мы вернём вас в главное меню";
            cancelProcess(appUser);
            output = TextMessageUtils.buildErrorMessage(botMessage);
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private String processBasicStateCommand(AppUser appUser, String text) {
        if (!text.startsWith(ServiceCommand.PREFIX)) {
            return "Я бы с удовольствие поговорил, " +
                    "но я просто бот, выполняющий команды ☺";
        }

        var serviceCommand = ServiceCommand.fromValue(text);
        if (serviceCommand == null) {
            String botMessage = "Команда не распознана!";
            return TextMessageUtils.buildErrorMessage(botMessage);
        }

        if (ServiceCommand.HELP.equals(serviceCommand)) {
            return "Список доступных команд:\n"
                    + "\n"
                    + ServiceCommand.CANCEL + " - отмена выполнения текущей команды";
        } else if (ServiceCommand.START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список " +
                    "доступных команд используйте " + ServiceCommand.HELP;
        } else {
            String botMessage = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже";
            return TextMessageUtils.buildErrorMessage(botMessage);
        }
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BotState.BASIC_STATE);
        appUserJpaRepository.save(appUser);
        return "Команда отменена!";
    }

    private void sendAnswer(String text, Long chatId) {
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
