package com.weather;

public class SubscriptionCommand {
    private Command command;
    private String topic;

    public SubscriptionCommand(Command command, String topic) {
        this.command = command;
        this.topic = topic;
    }

    public Command getCommand() {
        return command;
    }

    public String getTopic() {
        return topic;
    }
}
