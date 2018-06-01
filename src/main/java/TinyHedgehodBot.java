import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.Optional;

public class TinyHedgehodBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        Optional<Message> optional = Optional.ofNullable(update.getMessage());

        // We check if the update has a message and the message has text
//        optional.ifPresent(message -> {
//            SendMessage outgoing = null;
//
//            if (message.getLocation() != null) {
//                outgoing = new SendMessage()
//                        .setChatId(message.getChatId())
//                        .setText("Location reply");
//            } else if (message.getText() != null) {
//                outgoing = new SendMessage() // Create a SendMessage object with mandatory fields
//                        .setChatId(message.getChatId())
//                        .setText(message.getText());
//            }
//
//            send(outgoing);
//        });

        if (optional.isPresent()) {
            Message message = optional.get();
            SendMessage outgoing = null;

            if (message.getLocation() != null) {
                outgoing = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Location reply");
            } else if (message.getText() != null) {
                outgoing = new SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(message.getChatId())
                        .setText(message.getText());
            }

            send(outgoing);
        }
    }

    private void send(SendMessage outgoing) {
        if (outgoing != null) {
            try {
                execute(outgoing); // Call method to send the message
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
