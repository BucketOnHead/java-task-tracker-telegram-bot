package com.github.bucketonhead.service.processor.main.enums;

public enum AppCommand {
    // ===== GLOBAL
    START("start"),
    MAIN("main"),
    TASK_MODE("taskmode"),

    // ===== COMMON
    HELP("help"),
    BACK("back"),

    // ===== MAIN MODE
    PROFILE("profile"),

    // ===== TASK MODE
    NEW_TASK("task"),
    MY_TASKS("tasks"),
    DONE_TASK("donetask");

    public static final String PREFIX = "/";
    private final String value;

    AppCommand(String cmd) {
        this.value = PREFIX + cmd;
    }

    public static AppCommand fromValue(String value) {
        AppCommand foundCommand = null;
        for (var command : AppCommand.values()) {
            if (command.value.equals(value)) {
                foundCommand = command;
                break;
            }
        }

        return foundCommand;
    }

    public static boolean isCommandPattern(String str) {
        return (str != null) && str.startsWith(AppCommand.PREFIX);
    }

    @Override
    public String toString() {
        return value;
    }
}
