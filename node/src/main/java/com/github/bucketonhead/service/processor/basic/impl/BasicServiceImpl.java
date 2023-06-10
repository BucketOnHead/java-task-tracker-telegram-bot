package com.github.bucketonhead.service.processor.basic.impl;

import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.processor.basic.BasicService;
import com.github.bucketonhead.service.processor.main.enums.AppCommand;
import com.github.bucketonhead.service.sender.MessageSender;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class BasicServiceImpl implements BasicService {
    private final MessageSender msgSender;
    private final AppUserJpaRepository appUserJpaRepository;

    @Override
    public void processCommand(AppUser user, Message msg) {
        if (!AppCommand.isCommandPattern(msg.getText())) {
            processNotCommand(msg);
            return;
        }

        var cmd = AppCommand.parseAppCommand(msg.getText());
        if (cmd == null) {
            processBadCommand(msg);
        } else if (AppCommand.HELP == cmd) {
            processHelpCommand(msg);
        } else if (AppCommand.MAIN == cmd) {
            processMainCommand(user, msg);
        } else if (AppCommand.PROFILE == cmd) {
            processProfileCommand(user, msg);
        } else if (AppCommand.START == cmd) {
            processStartCommand(user, msg);
        } else if (AppCommand.TASK_MODE == cmd) {
            processTaskModeCommand(user, msg);
        } else {
            processNotImplemented(msg);
        }
    }

    private void processNotCommand(Message msg) {
        var text = "Я бы с удовольствие поговорил, " +
                "но я просто бот, выполняющий команды ☺";
        msgSender.send(text, msg.getChatId());
    }

    private void processBadCommand(Message msg) {
        var text = "Команда не распознана!";
        msgSender.sendError(text, msg.getChatId());
    }

    private void processNotImplemented(Message msg) {
        var text = "Если вы видите это сообщение, " +
                "значит разработчик забыл подключить " +
                "эту функциональность, попробуйте позже!";
        msgSender.sendError(text, msg.getChatId());
    }

    @Override
    public void processHelpCommand(Message msg) {
        var cmdDesc = new LinkedHashMap<AppCommand, String>();
        cmdDesc.put(AppCommand.PROFILE, "посмотреть свой профиль");
        cmdDesc.put(AppCommand.TASK_MODE, "перейти в режим управления задачами");
        cmdDesc.put(AppCommand.HELP, "получить список доступных команд");

        var text = ResponseMessageUtils.buildHelp(cmdDesc);
        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processMainCommand(AppUser user, Message msg) {
        String text;
        if (BotState.BASIC == user.getState()) {
            text = "Вы уже в главном меню 😉";
        } else {
            user.setState(BotState.BASIC);
            appUserJpaRepository.save(user);

            text = "Вернули вас в главное меню!";
        }

        msgSender.send(text, msg.getChatId());
    }

    private void processProfileCommand(AppUser user, Message msg) {
        var text = ResponseMessageUtils.buildProfileInfo(user);
        msgSender.sendParseMarkdown(text, msg.getChatId());
    }

    @Override
    public void processStartCommand(AppUser user, Message msg) {
        var regDuration = Duration.between(user.getFirstLoginDate(), LocalDateTime.now());
        if (regDuration.toSeconds() < 3) {
            processWelcome(msg);
        } else {
            processReturn(user, msg);
        }
    }

    private void processWelcome(Message msg) {
        var text = "Добро пожаловать 🥰\n\n" +
                "Используйте " + AppCommand.HELP +
                " чтобы узнать, что я умею 😊";
        msgSender.send(text, msg.getChatId());
    }

    // TODO: убрать удаление!!!
    private void processReturn(AppUser user, Message msg) {
        appUserJpaRepository.deleteById(user.getId());

        var text = "А я Вас помню 🙃\n\n" +
                "🧨 Обнулил ваш аккаунт 🧨";
        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processTaskModeCommand(AppUser user, Message msg) {
        String text;
        if (BotState.TASK_MODE == user.getState()) {
            text = "Вы уже в режиме задач 🙂";
        } else {
            user.setState(BotState.TASK_MODE);
            appUserJpaRepository.save(user);

            text = "Перевёл вас в режим управления задачами";
        }

        msgSender.send(text, msg.getChatId());
    }
}
