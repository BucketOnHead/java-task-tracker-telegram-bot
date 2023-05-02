package com.github.bucketonhead.service.enums;

public enum ServiceCommand {
    CANCEL("/cancel"),
    HELP("/help"),
    START("/start");
    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    public static ServiceCommand fromValue(String s) {
        ServiceCommand serviceCommand = null;
        for (ServiceCommand cmd : ServiceCommand.values()) {
            if (cmd.value.equals(s)) {
                serviceCommand = cmd;
                break;
            }
        }

        return serviceCommand;
    }

    @Override
    public String toString() {
        return value;
    }
}
