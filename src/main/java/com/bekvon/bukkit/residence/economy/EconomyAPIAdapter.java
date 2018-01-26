package com.bekvon.bukkit.residence.economy;

import me.onebone.economyapi.EconomyAPI;

/**
 * @author CreeperFace
 */
public class EconomyAPIAdapter implements EconomyInterface {

    private final EconomyAPI provider;

    public EconomyAPIAdapter() {
        this.provider = EconomyAPI.getInstance();
    }

    @Override
    public double getBalance(String playerName) {
        return provider.myMoney(playerName);
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        return provider.myMoney(playerName) >= amount;
    }

    @Override
    public boolean addMoney(String playerName, double amount) {
        return provider.addMoney(playerName, amount) == 1;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        return provider.reduceMoney(playerName, amount) == 1;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        double moneyFrom = provider.myMoney(playerFrom);
        if (moneyFrom < amount) {
            return false;
        }

        if (provider.addMoney(playerTo, amount) != 1) {
            return false;
        }

        return provider.reduceMoney(playerFrom, amount) == 1;
    }

    @Override
    public String getName() {
        return "EconomyAPI";
    }
}
