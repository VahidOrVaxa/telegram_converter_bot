package com.bot.service;

import com.bot.config.BotConfiguration;
import com.bot.logging.UpdateLoggingAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.DecimalFormat;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private boolean isLei = true;
    private final BotConfiguration config;
    private long globalChatId;
    private final Converter converter;
    private Currency currency;


    @Autowired
    public TelegramBot(BotConfiguration config, Converter converter) {
        super(config.getBotToken());
        this.config = config;
        this.converter = converter;
        currency = converter.getCurrency("usd");
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            changeValue(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().isCommand()) {
            UpdateLoggingAspect.beforeOnUpdateReceivedAdvice(update);
            Message message = update.getMessage();
            globalChatId = message.getChatId();
            String text = message.getText();

            switch (text) {
                case "/start" :
                    startCommandReceived();
                    break;
                case "/currency":
                    sendCurrency();
                    break;
                case "/change_value" :
                    menuValue();
                    break;
                case "/whats_new":
                    String answer = "Теперь данный бот не считает оценки, а конвертирует валюту.\n" +
                            "Чтобы испробовать данную функцию просто введите сумму в леях и бот переведет ее в доллары(по умолчанию).\n" +
                            "Чтобы изменить валюту, воспользуйтесь командой /change_value и выбирите валюту. " +
                            "Так же вы можете вывести все текущие валюты и их курс с помощью команды /currency. " +
                            "Еще вы можете изменит режим конвертации с помощью команды /change_mode, " +
                            "по умалчанию стоит режи LEI в USD. При какой-то недоработке пишите мне в лс @vazilinus";
                    sendMessage(answer);
                    break;
                case "/change_mode":
                    isLei = !isLei;
                    String response;
                    if (isLei) {
                        response = "Вводимая валюта была изменена на LEI. Конвертация проходит в " + currency.getName();
                    } else {
                        response = "Вводиамя валюта была изменена на " + currency.getName() + ". Конвертация проходит в LEI";
                    }
                    sendMessage(response);
                    break;
                case "/key404" :
                    System.exit(0);
                    break;
                default:
                    sendMessage("Кажется, я не знаю что на это ответить(((");
            }

        } else if (update.getMessage().hasText()) {
            globalChatId = update.getMessage().getChatId();
            UpdateLoggingAspect.beforeOnUpdateReceivedAdvice(update);
            try {
                DecimalFormat format = new DecimalFormat("######.##");
                float sum = Float.parseFloat(update.getMessage().getText());
                if (isLei) {
                    sum /= currency.getShell();
                    sendMessage("Результат: " + format.format(sum) + " " + currency.getName());
                } else {
                    sum *= currency.getBuy();
                    sendMessage("Результат: " + format.format(sum) + " LEI");
                }
            } catch (NumberFormatException e) {
                sendMessage("Кажется, вы ввели что-то не то(((");
            }
        }
        else {
            sendMessage("Sorry, what is this???");
        }
    }

    private void startCommandReceived() {
        String text = "Привет, это новая версия меня :)\n" +
                "Я могу перевести леи в какую либо другую валюту. Просто введи сумму и я выведу ее в валюте, которая тебе нужна. " +
                "Для ознакомления со списком команд введите команду /whats_new";
        sendMessage(text);
    }

    private void changeValue(CallbackQuery callbackQuery) {
        String text = callbackQuery.getData();
        if (text.equals("rub")) {
            String answer = "К сожалению на данный момент эта валюта не обслуживается. " +
                    "Валютой конвертации остается " + currency.getName();
            sendMessage(answer);
        } else {
            currency = converter.getCurrency(text);
            if (isLei) {
                sendMessage("Валюта в которую будут конвертированы леи была изменена на " + currency.getName());
            } else {
                sendMessage("Валюта которуа будет конвертироваться в леи была изменена на " + currency.getName());
            }
        }
        AnswerCallbackQuery answerCallbackQuery =
                AnswerCallbackQuery.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .build();
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }
    }

    private void menuValue() {
        InlineKeyboardMarkup keyboard;
        String text = "Выберите валюту конвертиции";

        var usd = InlineKeyboardButton.builder()
                .text("USD")
                .callbackData("usd")
                .build();

        var eur = InlineKeyboardButton.builder()
                .text("EUR")
                .callbackData("eur")
                .build();

        var rub = InlineKeyboardButton.builder()
                .text("RUB")
                .callbackData("rub")
                .build();

        var ron = InlineKeyboardButton.builder()
                .text("RON")
                .callbackData("ron")
                .build();

        var uah = InlineKeyboardButton.builder()
                .text("UAH")
                .callbackData("uah")
                .build();

        keyboard = InlineKeyboardMarkup
                .builder()
                .keyboardRow(List.of(usd, eur))
                .keyboardRow(List.of(rub, ron, uah))
                .build();

        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(globalChatId)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendCurrency() {
        String[] response = converter.currentValue();
        InlineKeyboardMarkup keyboardMarkup;
        InlineKeyboardButton url = InlineKeyboardButton
                .builder()
                .text("Сайт банка")
                .url(response[2])
                .build();

        String text = "Дата: " + response[0] + "\nВалюта/Покупка/Продажа/BNM\n" + response[1];
        keyboardMarkup = InlineKeyboardMarkup
                .builder()
                .keyboardRow(List.of(url))
                .build();

        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(globalChatId)
                .text(text)
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }
    }
    private void sendMessage(String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(globalChatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
