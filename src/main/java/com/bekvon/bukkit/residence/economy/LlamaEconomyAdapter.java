package com.bekvon.bukkit.residence.economy;

import net.lldv.llamaeconomy.LlamaEconomy;
import net.lldv.llamaeconomy.components.api.API;

public class LlamaEconomyAdapter implements EconomyInterface {

    private final API economy;

    public LlamaEconomyAdapter() {
        this.economy = LlamaEconomy.getAPI();
    }

    @Override
    public double getBalance(String playerName) {
        return this.economy.getMoney(playerName);
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        return this.economy.getMoney(playerName) >= amount;
    }

    @Override
    public boolean addMoney(String playerName, double amount) {
        this.economy.addMoney(playerName, amount);
        return true;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        this.economy.reduceMoney(playerName, amount);
        return true;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        this.economy.reduceMoney(playerFrom, amount);
        this.economy.addMoney(playerTo, amount);
        return true;
    }

    @Override
    public String getName() {
        return "LlamaEconomy";
    }
}
