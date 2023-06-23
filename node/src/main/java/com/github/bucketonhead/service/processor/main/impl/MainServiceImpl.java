package com.github.bucketonhead.service.processor.main.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.dao.RawDataJpaRepository;
import com.github.bucketonhead.entity.user.AppUser;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.entity.user.enums.BotState;
import com.github.bucketonhead.service.processor.basic.BasicService;
import com.github.bucketonhead.service.processor.main.MainService;
import com.github.bucketonhead.service.processor.main.enums.AppCommand;
import com.github.bucketonhead.service.processor.task.TaskService;
import com.github.bucketonhead.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
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

        var cmd = AppCommand.parseAppCommand(msg.getText());
        if (AppCommand.MAIN == cmd) {
            basicService.processMainCommand(appUser, msg);
            basicService.processHelpCommand(msg);
            return;
        } else if (AppCommand.TASK_MODE == cmd) {
            basicService.processTaskModeCommand(appUser, msg);
            taskService.processHelpCommand(msg);
            return;
        } else if (AppCommand.START == cmd) {
            basicService.processStartCommand(appUser, msg);
            return;
        } else if (AppCommand.DELETE == cmd) {
            basicService.processDeleteCommand(appUser, msg);
            return;
        }

        if (BotState.BASIC == appUser.getState()) {
            basicService.processCommand(appUser, msg);
        } else if (BotState.TASK_MODE == appUser.getState()) {
            taskService.processCommand(appUser, msg);
        } else if (BotState.WAIT_TASK == appUser.getState()) {
            taskService.processNewTaskCommand(appUser, msg);
        } else if (BotState.DONE_TASK == appUser.getState()) {
            taskService.processDoneTaskCommand(appUser, msg);
        } else {
            var text = "Разработчик допустил ошибку при реализации " +
                    "этой функциональности, попробуйте позже! " +
                    "А пока вернём вас в главное меню ☺";
            msgSender.sendError(text, msg.getChatId());
            basicService.processMainCommand(appUser, msg);
        }
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataJpaRepository.save(rawData);
    }

    private AppUser findOrSaveAppUser(User tgUser) {
        AppUser appUser;
        Optional<AppUser> persistenceAppUser = appUserJpaRepository.findByTelegramUserId(tgUser.getId());
        if (persistenceAppUser.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(tgUser.getId())
                    .firstName(tgUser.getFirstName())
                    .lastName(tgUser.getLastName())
                    .username(tgUser.getUserName())
                    .state(BotState.BASIC)
                    .build();
            appUser = appUserJpaRepository.save(transientAppUser);
        } else {
            appUser = persistenceAppUser.get();
        }

        return appUser;
    }
}
