package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class SellDyedArmorAdapter extends TradeJsonAdapter {

    @Override
    public @NotNull TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertString(json, "item");

        int price = json.get("price").getAsInt();
        Item item = VillagerJsonHelper.getItemFromID(json.get("item").getAsString());

        return new Factory(item, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final Item sell;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float priceMultiplier;

        public Factory(Item item, int price, int maxUses, int experience, float priceMultiplier) {
            this.sell = item;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack2 = new ItemStack(this.sell);
            if (itemStack2.isIn(ItemTags.DYEABLE)) {
                // Explicit type since var doesn't fully respect this
                List<DyeItem> list = Lists.newArrayList();
                list.add(getDye(random));
                if (random.nextFloat() > 0.7F) {
                    list.add(getDye(random));
                }

                if (random.nextFloat() > 0.8F) {
                    list.add(getDye(random));
                }

                itemStack2 = DyedColorComponent.setColor(itemStack2, list);
            }


            return new TradeOffer(CurrencyHelper.getClosestTradeItem(price), itemStack2, this.maxUses, this.experience, priceMultiplier);

        }

        private static DyeItem getDye(Random random) {
            return DyeItem.byColor(DyeColor.byId(random.nextInt(16)));
        }
    }
}
