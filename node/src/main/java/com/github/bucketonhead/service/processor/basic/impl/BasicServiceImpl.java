package com.github.bucketonhead.service.processor.basic.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.processor.basic.BasicService;
import com.github.bucketonhead.service.processor.main.enums.AppCommand;
import com.github.bucketonhead.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicServiceImpl implements BasicService {
    private final MessageSender msgSender;
    private final AppUserJpaRepository appUserJpaRepository;

    @Override
    public void processCommand(AppUser user, Message msg) {
        if (!AppCommand.isCommandPattern(msg.getText())) {
            var responseMessage = "Я бы с удовольствие поговорил, " +
                    "но я просто бот ☺";
            msgSender.send(responseMessage, msg.getChatId());
        }

        var command = AppCommand.fromValue(msg.getText());
        if (command == null) {
            var responseMessage = "Команда не распознана!";
            msgSender.sendError(responseMessage, msg.getChatId());
        }

        if (AppCommand.START == command) {
            processStartCommand(user, msg);
        } else if (AppCommand.HELP == command) {
            processHelpCommand(msg);
        } else if (AppCommand.MAIN == command) {
            processMainCommand(user, msg);
        } else if (AppCommand.TASK_MODE == command) {
            processTaskModeCommand(user, msg);
        } else {
            var text = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже!";
            msgSender.sendError(text, msg.getChatId());
        }
    }

    @Override
    public void processTaskModeCommand(AppUser user, Message msg) {
        user.setState(BotState.TASK_MODE);
        appUserJpaRepository.save(user);

        var responseMessage = "Перевёл вас в режим управления задачами";
        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processMainCommand(AppUser user, Message msg) {
        String responseMessage;
        if (BotState.BASIC == user.getState()) {
            responseMessage = "Вы уже в главном меню 😉";
        } else {
            user.setState(BotState.BASIC);
            appUserJpaRepository.save(user);
            responseMessage = "Вернули вас в главное меню!";
        }

        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processHelpCommand(Message msg) {
        Map<AppCommand, String> commandDescription = new LinkedHashMap<>();
        commandDescription.put(AppCommand.TASK_MODE, "перейти в режим управления задачами");
        commandDescription.put(AppCommand.HELP, "получить список доступных команд");

        var responseMessage = "Список доступных команд:" + commandDescription.entrySet()
                .stream()
                .map(entry -> String.format("%n%n%s - %s.", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());
        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processStartCommand(AppUser user, Message msg) {
        String responseMessage;
        var regDuration = Duration.between(user.getFirstLoginDate(), LocalDateTime.now());
        if (regDuration.toSeconds() < 3) {
            responseMessage = "" +
                    "Добро пожаловать 🥰\n" +
                    "\n" +
                    "Используйте " + AppCommand.HELP +
                    " чтобы узнать, что я умею 😊";
        } else {
            // TODO: убрать удаление!!!
            responseMessage = "А я Вас помню 🙃\n\n" +
                    "🧨 Обнулил ваш аккаунт 🧨";
            appUserJpaRepository.deleteById(user.getId());
        }

        msgSender.send(responseMessage, msg.getChatId());
    }
}