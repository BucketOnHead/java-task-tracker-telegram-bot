package com.github.bucketonhead.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextMessageUtils {
    private final String ERROR_MESSAGE_PATTERN = "⚠  Ошибка%n%n%s";

    public String buildErrorMessage(String text) {
        return String.format(ERROR_MESSAGE_PATTERN, text);
    }
}
