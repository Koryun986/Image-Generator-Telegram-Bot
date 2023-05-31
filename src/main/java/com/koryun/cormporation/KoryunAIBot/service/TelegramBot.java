package com.koryun.cormporation.KoryunAIBot.service;

import com.koryun.cormporation.KoryunAIBot.config.BotConfig;
import com.koryun.cormporation.KoryunAIBot.config.OpenAIConfig;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot (BotConfig config) {
        this.config = config;
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getUserName();

            switch (messageText) {
                case "/start" :
                    startCommandHandlingAndSendingMessage(chatId, userName);
                    break;
                default:
                    recievedImageDescription(chatId, messageText);
            }
        }

    }

    private void startCommandHandlingAndSendingMessage(long chatId, String name) {
        String answer = "Hi " + name + "\nWrite image description and AI send you generated image" ;

        sendMessage(chatId, answer);
    }

    private void recievedImageDescription (long chatId, String message) {
        OpenAiService service = new OpenAiService(OpenAIConfig.OPEN_AI_TOKEN);

        CreateImageRequest request = CreateImageRequest.builder()
                .prompt(message)
                .n(1)
                .size("512x512")
                .build();
        List<Image> response = service.createImage(request).getData();

        sendMessage(chatId, response.toString());
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        }catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
