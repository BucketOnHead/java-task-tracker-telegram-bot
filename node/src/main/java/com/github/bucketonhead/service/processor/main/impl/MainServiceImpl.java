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
import com.github.bucketonhead.service.sender.MessageSender;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final MessageSender msgSender;
    private final RawDataJpaRepository rawDataJpaRepository;
    private final AppUserJpaRepository appUserJpaRepository;
    private final BasicService basicService;
    private final TaskService taskService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var msg = update.getMessage();
        var appUser = findOrSaveAppUser(msg.getFrom());
        String responseMessage;

        var serviceCommand = ServiceCommand.fromValue(msg.getText());
        if (ServiceCommand.CANCEL.equals(serviceCommand)) {
            responseMessage = basicService.processCancelCommand(appUser);
            msgSender.sendText(responseMessage, msg.getChatId());

            responseMessage = basicService.processHelpCommand();
            msgSender.sendText(responseMessage, msg.getChatId());
            return;
        }

        if (BotState.BASIC == appUser.getState()) {
            responseMessage = basicService.processCommand(appUser, msg);
            msgSender.sendText(responseMessage, msg.getChatId());

            responseMessage = basicService.processHelpCommand();
            msgSender.sendText(responseMessage, msg.getChatId());
            return;
        } else if (BotState.TASK_MODE == appUser.getState()) {
            responseMessage = taskService.processCommand(appUser, msg);
        } else if (BotState.WAIT_TASK == appUser.getState()) {
            responseMessage = taskService.processNewTaskCommand(appUser, msg);
        } else if (BotState.DONE_TASK == appUser.getState()) {
            responseMessage = taskService.processDoneTaskCommand(appUser, msg);
            msgSender.sendText(responseMessage, msg.getChatId());

            responseMessage = taskService.processMyTasksCommand(appUser);
            msgSender.sendText(responseMessage, msg.getChatId());
            return;
        } else {
            log.error("state: {}, не реализован", appUser.getState());
            var text = "Разработчик допустил ошибку при реализации " +
                    "этой функциональности, попробуйте позже! " +
                    "А пока вернём вас в главное меню ☺";
            responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            msgSender.sendText(responseMessage, msg.getChatId());

            responseMessage = basicService.processCancelCommand(appUser);
            msgSender.sendText(responseMessage, msg.getChatId());
            return;
        }

        msgSender.sendText(responseMessage, msg.getChatId());
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
