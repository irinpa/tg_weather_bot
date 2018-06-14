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

public class TinyHedgehodBot extends TelegramLongPollingBot {
    private final WeatherApiClient weatherApi;
    private final Map<Long, String> subscriptions;

    public TinyHedgehodBot() {
        weatherApi = new WeatherApiClient();
        subscriptions = new ConcurrentHashMap<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
//            System.out.println("=====");

            for (Map.Entry<Long, String> entry : subscriptions.entrySet()) {
                try {
                    String forecast = weatherApi.getForecastByText(entry.getValue());

                    SendMessage message = new SendMessage()
                            .setChatId(entry.getKey())
                            .setText(forecast);

                    send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                System.out.println("Subscriber " + entry.getKey() + " for " + entry.getValue());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }


    @Override
    public void onUpdateReceived(Update update) {
        Optional<Message> optional = Optional.ofNullable(update.getMessage());

        if (optional.isPresent()) {
            Message message = optional.get();
            SendMessage outgoing = null;

            try {
                if (message.getLocation() != null) {

                    StringBuilder response = new StringBuilder();
                    response.append(weatherApi.getWeatherByLocation(message.getLocation().getLatitude(),
                            message.getLocation().getLongitude())).append("\n")
                            .append(weatherApi.getForecastByLocation(message.getLocation().getLatitude(),
                                    message.getLocation().getLongitude()));
                    /*String response1 = weatherApi.getWeatherByLocation(message.getLocation().getLatitude(),
                            message.getLocation().getLongitude());
                    String response2 = weatherApi.getForecastByLocation((message.getLocation().getLatitude(),
                            message.getLocation().getLongitude());*/
                    outgoing = new SendMessage()
                            .setChatId(message.getChatId())
                            .setText(String.valueOf(response));
                } else if (message.getText() != null) {
                    Subscription subscription = getCommand(message.getText());
                    if (subscription != null) {
                        processCommand(subscription, message);
                    } else {
                        StringBuilder response = new StringBuilder();
                        response.append(weatherApi.getWeatherByText(message.getText())).append("\n")
                                .append(weatherApi.getForecastByText(message.getText()));
                        /*String response = weatherApi.getWeatherByText(message.getText());*/
                        outgoing = new SendMessage()
                                .setChatId(message.getChatId())
                                .setText(String.valueOf(response));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            send(outgoing);
        }
    }

    private void processCommand(Subscription subscription, Message message) {
        switch (subscription.getCommand()) {
            case subscribe:
                subscriptions.put(message.getChatId(), subscription.getTopic());
                SendMessage outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Congratulations! You have just subscribed to " + subscription.getTopic() + " weather forecast.");
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
    private Subscription getCommand(String text) {
        if (text.startsWith("/subscribe")) {
            // TODO validate text for length
            // send warning instead of subscription
            String topic = text.substring(11);
            return new Subscription(Command.subscribe, topic);
            /*return Command.subscribe;*/
        } else if (text.equals("/unsubscribe")) {
            return new Subscription(Command.unsubscribe, null);
           /* return Command.unsubscribe;*/
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
