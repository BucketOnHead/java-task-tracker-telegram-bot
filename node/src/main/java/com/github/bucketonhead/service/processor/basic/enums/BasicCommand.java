package com.github.bucketonhead.service.processor.basic.enums;

public enum BasicCommand {
    START("start"),
    HELP("help"),
    MAIN_MODE("main"),
    TASK_MODE("taskmode");
    public static final String PREFIX = "/";
    private final String command;

    BasicCommand(String command) {
        this.command = PREFIX + command;
    }

    public static BasicCommand fromValue(String value) {
        BasicCommand basicCommand = null;
        for (BasicCommand cmd : BasicCommand.values()) {
            if (cmd.command.equals(value)) {
                basicCommand = cmd;
                break;
            }
        }

        return basicCommand;
    }

    public static boolean isCommandPattern(String str) {
        return (str != null) && str.startsWith(BasicCommand.PREFIX);
    }

    @Override
    public String toString() {
        return command;
    }
}
