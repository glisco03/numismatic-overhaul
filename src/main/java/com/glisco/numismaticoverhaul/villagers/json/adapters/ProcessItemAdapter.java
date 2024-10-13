package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public class ProcessItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "buy");
        VillagerJsonHelper.assertJsonObject(json, "sell");

        ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
        ItemStack buy = VillagerJsonHelper.getItemStackFromJson(json.get("buy").getAsJsonObject());

        int price = json.get("price").getAsInt();

        return new Factory(buy, sell, price, max_uses, villager_experience, price_multiplier);
    }

    private record Factory(ItemStack buy, ItemStack sell, int price, int maxUses, int experience,
                           float multiplier) implements TradeOffers.Factory {

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(CurrencyHelper.getClosestTradeItem(price), Optional.of(new TradedItem(buy.getItem(), buy.getCount())), sell, this.maxUses, this.experience, this.multiplier);
        }
    }
}
