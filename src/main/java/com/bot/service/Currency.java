package com.bot.service;

import java.util.Objects;

public class Currency {
    private String name;
    private float buy;
    private float shell;
    private float bnm;

    public Currency(){}

    public Currency(String name) {
        this.name = name;
    }

    public Currency(String name, float buy, float shell, float bnm) {
        this.name = name;
        this.buy = buy;
        this.shell = shell;
        this.bnm = bnm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBuy() {
        return buy;
    }

    public void setBuy(float buy) {
        this.buy = buy;
    }

    public float getShell() {
        return shell;
    }

    public void setShell(float shell) {
        this.shell = shell;
    }

    public float getBnm() {
        return bnm;
    }

    public void setBnm(float bnm) {
        this.bnm = bnm;
    }

    @Override
    public String toString() {
        return name + " : " +
                buy + " / " +
                shell + " / " +
                bnm + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Currency currency = (Currency) o;
        return name.equals(currency.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
