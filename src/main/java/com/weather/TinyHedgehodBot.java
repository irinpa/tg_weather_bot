package com.weather;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TinyHedgehodBot extends TelegramLongPollingBot {
    private final WeatherApiClient weatherApi;
    private final SubscriptionDAO dao;

    public TinyHedgehodBot(SubscriptionDAO dao) {
        weatherApi = new WeatherApiClient();
        this.dao = dao;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
//            System.out.println("=====");

            for (Subscription entry : this.dao.getAll()) {
                try {
                    String forecast = weatherApi.getForecastByText(entry.getTopic());

                    SendMessage message = new SendMessage()
                            .setChatId(entry.getChatId())
                            .setText(forecast);

                    send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                System.out.println("Subscriber " + entry.getKey() + " for " + entry.getValue());
            }
        }, 0, 24, TimeUnit.HOURS);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Optional<Message> optional = Optional.ofNullable(update.getMessage());

        if (optional.isPresent()) {
            Message message = optional.get();
            SendMessage outgoing = null;

            try {
                if (message.getLocation() != null) {

                    Float latitude = message.getLocation().getLatitude();
                    Float longitude = message.getLocation().getLongitude();

                    String currentWeather = weatherApi.getWeatherByLocation(latitude, longitude);
                    String forecast = weatherApi.getForecastByLocation(latitude, longitude);

                    String response = currentWeather + "\n" + forecast;

                    outgoing = new SendMessage()
                            .setChatId(message.getChatId())
                            .setText(response);
                } else if (message.getText() != null) {
                    SubscriptionCommand subscription = getCommand(message.getText());
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

    private void processCommand(SubscriptionCommand subscription, Message message) {
        switch (subscription.getCommand()) {
            case subscribe:
                dao.put(message.getChatId(), subscription.getTopic());
                SendMessage outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Congratulations! You have just subscribed to " + subscription.getTopic() + " weather forecast.");
                send(outgoing);

                break;
            case unsubscribe:
                dao.remove(message.getChatId());
                outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Sorry to see you leave :(");
                send(outgoing);
                break;
        }
    }

    // "/subscribe london"
    private SubscriptionCommand getCommand(String text) {
        if (text.startsWith("/subscribe")) {
            if (text.length() <= 10) {
                System.out.println("Please try again. The right format is [/subscribe City name], e.g. [/subscribe Moscow]");
                return null;
            } else {
                String topic = text.substring(11);
                return new SubscriptionCommand(Command.subscribe, topic);
            }
        } else if (text.equals("/unsubscribe")) {
            return new SubscriptionCommand(Command.unsubscribe, null);
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
