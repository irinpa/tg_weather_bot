package com.weather;

public class Subscription {
    private Command command;
    private String topic;

    public Subscription(Command command, String topic) {
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
