package com.weather;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TinyHedgehogBot extends TelegramLongPollingBot {
    private final WeatherApiClient weatherApi;
    private final Map<Long, String> subscriptions;

    public TinyHedgehogBot() {
        weatherApi = new WeatherApiClient();
        subscriptions = new ConcurrentHashMap<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            System.out.println("=====");

            for (Map.Entry<Long, String> entry : subscriptions.entrySet()) {
                String forecast = null;
                try {
                    forecast = weatherApi.getForecastByText(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SendMessage message = new SendMessage().setChatId(entry.getKey()).setText(forecast);
                send(message);

                System.out.println("Subscriber " + entry.getKey() + " for " + entry.getValue());

            }
        }, 0, 5, TimeUnit.SECONDS);
    }


    @Override
    public void onUpdateReceived(Update update) {
        Optional<Message> optional = Optional.ofNullable(update.getMessage());

        if (optional.isPresent()) {
            Message message = optional.get();
            SendMessage outgoing = null;

            try {
                if (message.getLocation() != null) {
                    String response = weatherApi.getWeatherByLocation(message.getLocation().getLatitude(),
                            message.getLocation().getLongitude());
                    outgoing = new SendMessage()
                            .setChatId(message.getChatId())
                            .setText(response);
                } else if (message.getText() != null) {
                    Command command = getCommand(message.getText());
                    if (command != null) {
                        processCommand(command, message);
                    } else {
                        String response = weatherApi.getWeatherByText(message.getText());
                        outgoing = new SendMessage()
                                .setChatId(message.getChatId())
                                .setText(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            send(outgoing);
        }
    }

    private void processCommand(Command command, Message message) {
        switch (command) {
            case subscribe:
                subscriptions.put(message.getChatId(), topic);
                SendMessage outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Congratulations! You have just subscribed to" + topic + "forecast");
                send(outgoing);

                break;
            case unsubscribe:
                subscriptions.remove(message.getChatId());
                outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Sorry to see you leave :(");
                send(outgoing);
                break;
        }
    }

    // "/subscribe london"
    private Command getCommand(String text) {
        if (text.startsWith("/subscribe")) {
            // TODO return new Subscription(command, topic)
            String topic = text.substring(11);
            return Command.subscribe;
        } else if (text.equals("/unsubscribe")) {
            return Command.unsubscribe;
        } else {
            return null;
        }
    }


    private void send(SendMessage outgoing) {
        if (outgoing != null) {
            try {
                execute(outgoing);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public String getBotUsername() {
        return "TinyHedgeHodBot";
    }

    @Override
    public String getBotToken() {
        return "573840977:AAHP81xFb9KyJgJodP8tVvjI2bm2-LE7gBw";
    }
}
