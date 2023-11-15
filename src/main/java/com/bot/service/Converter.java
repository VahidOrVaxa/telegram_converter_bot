package com.bot.service;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Converter {
//    @Value("${converter.url}")
    private String URL = "https://www.maib.md/ru/curs-valutar";
    private final String[] value;
    private Map<String, Currency> courses;
    private String data;

    public Converter() {
        value = new String[]{"USD", "EUR", "RUB", "RON", "UAH", "GBP"};
        courses = new LinkedHashMap<>();
    }

    @PostConstruct
    @Scheduled(fixedDelayString = "${interval}")
    public void initialize() {
        Document document;
        boolean trigger = true;

        try {
            document = Jsoup.connect(URL).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] text = document.text().split(" ");

        int j = 0;
        for (int i = 0; i < text.length; i++) {
            if (text[i].equals("BNM") && trigger) {
                data = text[i-5];
                trigger = false;
            } else if (text[i].equals(value[j]) && j < value.length-1) {
                if (!text[i+1].equals("-")) {
                    courses.put(value[j++], new Currency(text[i], Float.parseFloat(text[i + 1]),
                            Float.parseFloat(text[i + 2]), Float.parseFloat(text[i + 3])));
                } else
                    courses.put(value[j++], new Currency(text[i]));
                i+=3;
            }
        }
    }

    public String[] currentValue() {
        StringBuilder response = new StringBuilder("");
        for (Currency currency : courses.values()) {
            response.append(currency);
        }
        return new String[]{data, response.toString(), URL};
    }

    public Currency getCurrency(String currency) {
        return courses.get(currency.toUpperCase());
    }

}

