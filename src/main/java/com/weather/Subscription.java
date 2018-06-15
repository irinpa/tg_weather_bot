package com.weather;

public class Subscription {
    private Long chatId;
    private String topic;


    public Subscription(Long chatId, String topic) {
        this.chatId = chatId;
        this.topic = topic;

    }

    public Long getChatId() {
        return chatId;
    }

    public String getTopic() {
        return topic;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
