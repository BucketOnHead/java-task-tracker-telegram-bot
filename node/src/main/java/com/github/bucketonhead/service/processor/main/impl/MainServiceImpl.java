package com.github.bucketonhead.service.processor.main.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.dao.RawDataJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.processor.basic.BasicService;
import com.github.bucketonhead.service.processor.main.MainService;
import com.github.bucketonhead.service.processor.main.enums.ServiceCommand;
import com.github.bucketonhead.service.processor.task.TaskService;
import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.utils.ResponseMessageUtils;
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
    private final BasicService basicService;
    private final TaskService taskService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var msg = update.getMessage();
        var appUser = findOrSaveAppUser(msg.getFrom());

        var serviceCommand = ServiceCommand.fromValue(msg.getText());
        if (ServiceCommand.CANCEL.equals(serviceCommand)) {
            var responseMessage = basicService.processCancelCommand(appUser);
            sendResponseMessage(responseMessage, msg.getChatId());

            responseMessage = basicService.processHelpCommand();
            sendResponseMessage(responseMessage, msg.getChatId());
            return;
        }

        if (BotState.BASIC == appUser.getState()) {
            basicService.processCommand(appUser, msg);
        } else if (BotState.TASK_MODE == appUser.getState()) {
            taskService.processCommand(appUser, msg);
        } else if (BotState.WAIT_TASK == appUser.getState()) {
            var responseMessage = taskService.processNewTaskCommand(appUser, msg);
            sendResponseMessage(responseMessage, msg.getChatId());
        } else if (BotState.DONE_TASK == appUser.getState()) {
            var responseMessage = taskService.processDoneTaskCommand(appUser, msg);
            sendResponseMessage(responseMessage, msg.getChatId());

            responseMessage = taskService.processMyTasksCommand(appUser);
            sendResponseMessage(responseMessage, msg.getChatId());
        } else {
            log.error("state: {}, не реализован", appUser.getState());
            var text = "Разработчик допустил ошибку при реализации " +
                    "этой функциональности, попробуйте позже! " +
                    "А пока вернём вас в главное меню ☺";
            var responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            sendResponseMessage(responseMessage, msg.getChatId());

            responseMessage = basicService.processCancelCommand(appUser);
            sendResponseMessage(responseMessage, msg.getChatId());
        }
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
                    .state(BotState.BASIC)
                    .build();
            persistenceAppUser = appUserJpaRepository.save(transientAppUser);
        }
        return persistenceAppUser;
    }
}
