package com.github.bucketonhead.utils;

import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.service.processor.main.enums.AppCommand;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@UtilityClass
public class ResponseMessageUtils {
    private final String ERROR_PATTERN = "" +
            "⚠  Ошибка%n" +
            "%n" +
            "%s";
    private final String PROFILE_INFO_PATTERN = "" +
            "Ваш профиль ⚡%n" +
            "%n" +
            "ID:  `%d`%n" +
            "Ник:  %s%n" +
            "Имя:  %s%n" +
            "%n" +
            "Вы с нами уже %d дн! ♥";

    public String buildError(String text) {
        return String.format(ERROR_PATTERN, text);
    }

    public String buildProfileInfo(AppUser user) {
        long id = user.getTelegramUserId();
        String username = getUsername(user);
        String fullName = getFullName(user);
        Duration duration = Duration.between(user.getFirstLoginDate(), LocalDateTime.now());

        return String.format(PROFILE_INFO_PATTERN, id, username, fullName, duration.toDays());
    }

    private static String getUsername(AppUser user) {
        String username = user.getUsername();
        if (StringUtils.isBlank(username)) {
            username = "Не указан";
        }

        return username;
    }

    private static String getFullName(AppUser user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String fullName;
        if (!StringUtils.isBlank(firstName) && !StringUtils.isBlank(lastName)) {
            fullName = String.format("%s %s", firstName, lastName);
        } else if (!StringUtils.isBlank(firstName)) {
            fullName = firstName;
        } else if (!StringUtils.isBlank(lastName)) {
            fullName = lastName;
        } else {
            fullName = "Не указано";
        }

        return fullName;
    }

    @SuppressWarnings("java:S1319")
    public String buildHelp(LinkedHashMap<AppCommand, String> cmdDesc) {
        return "Список доступных команд:" + cmdDesc.entrySet()
                .stream()
                .map(entry -> String.format(
                        "%n%n%s - %s.",
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.joining());
    }
}
