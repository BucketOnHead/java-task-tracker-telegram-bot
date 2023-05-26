package com.github.bucketonhead.service.command.main.enums;

public enum ServiceCommand {
    CANCEL("cancel"),
    HELP("help"),
    NEW_TASK("task"),
    MY_TASKS("mytasks"),
    DONE_TASK("donetask"),
    START("start");
    public static final String PREFIX = "/";
    private final String command;

    ServiceCommand(String command) {
        this.command = PREFIX + command;
    }

    public static ServiceCommand fromValue(String value) {
        ServiceCommand serviceCommand = null;
        for (ServiceCommand cmd : ServiceCommand.values()) {
            if (cmd.command.equals(value)) {
                serviceCommand = cmd;
                break;
            }
        }

        return serviceCommand;
    }

    @Override
    public String toString() {
        return command;
    }
}
