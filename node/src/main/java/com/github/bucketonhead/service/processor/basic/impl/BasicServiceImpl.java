package com.github.bucketonhead.service.processor.basic.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.processor.basic.BasicService;
import com.github.bucketonhead.service.processor.basic.enums.BasicCommand;
import com.github.bucketonhead.service.processor.task.TaskService;
import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    private final ProducerService producerService;
    private final AppUserJpaRepository appUserJpaRepository;
    private final TaskService taskService;

    @Override
    public void processCommand(AppUser user, Message msg) {
        if (!BasicCommand.isCommandPattern(msg.getText())) {
            var responseMessage = "Я бы с удовольствие поговорил, " +
                    "но я просто бот ☺";
            sendMessage(responseMessage, msg.getChatId());
            return;
        }

        var command = BasicCommand.fromValue(msg.getText());
        if (command == null) {
            var text = "Команда не распознана!";
            var responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            sendMessage(responseMessage, msg.getChatId());
            return;
        }

        String responseMessage;
        if (BasicCommand.START == command) {
            responseMessage = processStartCommand(user);
        } else if (BasicCommand.HELP == command) {
            responseMessage = processHelpCommand();
        } else if (BasicCommand.CANCEL == command) {
            responseMessage = processCancelCommand(user);
        } else if (BasicCommand.TASK_MODE == command) {
            responseMessage = processTaskModeCommand(user);
            sendMessage(responseMessage, msg.getChatId());

            responseMessage = taskService.processHelpCommand();
            sendMessage(responseMessage, msg.getChatId());
            return;
        } else {
            var text = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже!";
            responseMessage = ResponseMessageUtils.buildErrorMessage(text);
        }

        sendMessage(responseMessage, msg.getChatId());
    }

    private String processTaskModeCommand(AppUser user) {
        user.setState(BotState.TASK_MODE);
        appUserJpaRepository.save(user);
        return "Текущий режим: управление задачами";
    }

    @Override
    public String processCancelCommand(AppUser user) {
        String responseMessage;
        if (BotState.BASIC == user.getState()) {
            responseMessage = "Вы уже в главном меню 😉";
        } else {
            user.setState(BotState.BASIC);
            appUserJpaRepository.save(user);
            responseMessage = "Вернули вас в главное меню!";
        }

        return responseMessage;
    }

    @Override
    public String processHelpCommand() {
        Map<BasicCommand, String> commandDescription = new LinkedHashMap<>();
        commandDescription.put(BasicCommand.HELP, "получить список доступных команд");
        commandDescription.put(BasicCommand.TASK_MODE, "перейти в режим управления задачами");
        commandDescription.put(BasicCommand.CANCEL, "отмена выполнения текущей команды");

        return "Список доступных команд:" + commandDescription.entrySet()
                .stream()
                .map(entry -> String.format("%n%n%s - %s.", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());
    }

    @Override
    public String processStartCommand(AppUser user) {
        String responseMessage;
        var regDuration = Duration.between(user.getFirstLoginDate(), LocalDateTime.now());
        if (regDuration.toSeconds() < 3) {
            responseMessage = "Добро пожаловать 🥰\n\n" +
                    "Используйте " + BasicCommand.HELP +
                    " чтобы узнать, что я умею 😊";
        } else {
            // TODO: убрать удаление!!!
            responseMessage = "А я Вас помню 🙃\n\n" +
                    "🧨 Обнулил ваш аккаунт 🧨";
            appUserJpaRepository.deleteById(user.getId());
        }

        return responseMessage;
    }

    private void sendMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        producerService.producerAnswer(sendMessage);
    }
}
