package com.github.bucketonhead.service.processor.task.enums;

import com.github.bucketonhead.service.processor.basic.enums.BasicCommand;

public enum TaskCommand {
    HELP("help"),
    NEW_TASK("task"),
    MY_TASKS("tasks"),
    DONE_TASK("donetask");
    public static final String PREFIX = "/";
    private final String command;

    TaskCommand(String command) {
        this.command = PREFIX + command;
    }

    public static TaskCommand fromValue(String value) {
        TaskCommand taskCommand = null;
        for (TaskCommand cmd : TaskCommand.values()) {
            if (cmd.command.equals(value)) {
                taskCommand = cmd;
                break;
            }
        }

        return taskCommand;
    }

    public static boolean isCommandPattern(String str) {
        return (str != null) && str.startsWith(BasicCommand.PREFIX);
    }

    @Override
    public String toString() {
        return command;
    }
}
