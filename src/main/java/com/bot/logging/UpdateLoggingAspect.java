package com.bot.logging;


import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.FileWriter;
import java.io.IOException;

public class UpdateLoggingAspect {
    private static FileWriter fileWriter;
    public static void beforeOnUpdateReceivedAdvice(Update update){
        try {
            fileWriter = new FileWriter("D:/demo/spring_course/telegram_converter_bot/src/main/resources/logs.txt", true);
            fileWriter.append(update.getMessage().getFrom().getUserName() + ", "
                    + update.getMessage().getFrom().getFirstName()  + ", "
                    + update.getMessage().getFrom().getLastName() + ": "+ update.getMessage().getText() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
