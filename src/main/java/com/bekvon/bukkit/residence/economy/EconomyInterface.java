package com.bekvon.bukkit.residence.economy;

public interface EconomyInterface {

    double getBalance(String playerName);

    boolean canAfford(String playerName, double amount);

    boolean addMoney(String playerName, double amount);

    boolean subtract(String playerName, double amount);

    boolean transfer(String playerFrom, String playerTo, double amount);

    String getName();
}
