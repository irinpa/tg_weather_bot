import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TinyHedgehodBot extends TelegramLongPollingBot {

    private final Send_HTTP_Request weatherApi;
    private final Set<Long> subscriptions;
    private final ScheduledExecutorService executor;


    public TinyHedgehodBot() {
        weatherApi = new Send_HTTP_Request();
        subscriptions = Collections.synchronizedSet(new HashSet<>());
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("=====");
                for (Long subscription : subscriptions) {
                    System.out.println("Subscriber " + subscription);
                }
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
                    String response = weatherApi.getByLocation(message.getLocation().getLatitude(),
                            message.getLocation().getLongitude());
                    outgoing = new SendMessage()
                            .setChatId(message.getChatId())
                            .setText(response);
                } else if (message.getText() != null) {
                    Command command = getCommand(message.getText());
                    if (command != null) {
                        processCommand(command, message);
                    } else {
                        String response = weatherApi.getByText(message.getText());
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
                subscriptions.add(message.getChatId());
                SendMessage outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Congrats!");
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

    private Command getCommand(String text) {
        if (text.equals("/subscribe")) {
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
