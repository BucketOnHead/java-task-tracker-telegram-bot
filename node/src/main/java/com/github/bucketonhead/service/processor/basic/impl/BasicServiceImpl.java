package com.github.bucketonhead.service.processor.basic.impl;

import com.github.bucketonhead.cache.AppCache;
import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.processor.basic.BasicService;
import com.github.bucketonhead.service.processor.main.enums.AppCommand;
import com.github.bucketonhead.service.sender.MessageSender;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicServiceImpl implements BasicService {
    private final MessageSender msgSender;
    private final AppUserJpaRepository appUserJpaRepository;
    private final AppCache<Long, AppUser> appUserCache;

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
        log.info("Processing not command");
        var text = "Я бы с удовольствие поговорил, " +
                "но я просто бот, выполняющий команды ☺";
        msgSender.send(text, msg.getChatId());
    }

    private void processBadCommand(Message msg) {
        log.info("Processing bad command");
        var text = "Команда не распознана!";
        msgSender.sendError(text, msg.getChatId());
    }

    private void processNotImplemented(Message msg) {
        log.error("Processing not implemented");
        var text = "Если вы видите это сообщение, " +
                "значит разработчик забыл подключить " +
                "эту функциональность, попробуйте позже!";
        msgSender.sendError(text, msg.getChatId());
    }

    @Override
    public void processHelpCommand(Message msg) {
        log.info("Processing help command");
        var cmdDesc = new LinkedHashMap<AppCommand, String>();
        cmdDesc.put(AppCommand.PROFILE, "посмотреть свой профиль");
        cmdDesc.put(AppCommand.TASK_MODE, "перейти в режим управления задачами");
        cmdDesc.put(AppCommand.HELP, "получить список доступных команд");

        var text = ResponseMessageUtils.buildHelp(cmdDesc);
        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processMainCommand(AppUser user, Message msg) {
        log.info("Processing main command");
        String text;
        if (BotState.BASIC == user.getState()) {
            text = "Вы уже в главном меню 😉";
        } else {
            user.setState(BotState.BASIC);
            var savedUser = appUserJpaRepository.save(user);
            appUserCache.put(savedUser);

            text = "Вернули вас в главное меню!";
        }

        msgSender.send(text, msg.getChatId());
    }

    private void processProfileCommand(AppUser user, Message msg) {
        log.info("Processing profile command");
        var text = ResponseMessageUtils.buildProfileInfo(user);
        msgSender.sendParseMarkdown(text, msg.getChatId());
    }

    @Override
    public void processStartCommand(AppUser user, Message msg) {
        log.info("Processing start command");
        var regDuration = Duration.between(user.getFirstLoginDate(), LocalDateTime.now());
        if (regDuration.toSeconds() < 3) {
            processWelcome(msg);
        } else {
            processReturn(msg);
        }
    }

    private void processWelcome(Message msg) {
        log.info("Processing welcome");
        var text = "Добро пожаловать 🥰\n\n" +
                "Используйте " + AppCommand.HELP +
                " чтобы узнать, что я умею 😊";
        msgSender.send(text, msg.getChatId());
    }

    private void processReturn(Message msg) {
        log.info("Processing return");
        var text = "А я Вас помню 🙃\n\n" +
                "Используйте " + AppCommand.DELETE +
                ", если хотите начать всё с чистого листа ☠\n\n" +
                "❗ Будьте внимательны восстановить аккаунт " +
                "будет невозможно 😱";
        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processTaskModeCommand(AppUser user, Message msg) {
        log.info("Processing task mode command");
        String text;
        if (BotState.TASK_MODE == user.getState()) {
            text = "Вы уже в режиме задач 🙂";
        } else {
            user.setState(BotState.TASK_MODE);
            var savedUser = appUserJpaRepository.save(user);
            appUserCache.put(savedUser);

            text = "Перевёл вас в режим управления задачами";
        }

        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processDeleteCommand(AppUser user, Message msg) {
        log.info("Processing delete command");
        appUserJpaRepository.deleteById(user.getId());
        appUserCache.remove(user.getTelegramUserId());

        var text = "Готово! С этого момента " +
                "мы больше не знакомы 😔\n\n";
        msgSender.send(text, msg.getChatId());
    }
}
