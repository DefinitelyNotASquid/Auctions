package com.mith.Auctions.object;

import org.bukkit.Material;

public class AverageItem {
    private double amount;
    private double price;
    private Material type;

    public AverageItem(double amount, double price, Material type) {
        this.amount = amount;
        this.price = price;
        this.type = type;
    }

    public Material getMaterial() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public double getAveragePrice()
    {
        if (amount == 0 || price == 0)
        {
            return 0;
        }
        return (price/amount);
    }

    public void setAmount(double amount) {
       this.amount = amount;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void updateAmount(double amount)
    {
        this.amount = this.amount + amount;
    }
    public void updatePrice(double price)
    {
        this.price = this.price + price;
    }
}
