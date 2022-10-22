package com.glisco.numismaticoverhaul.villagers.data;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.villagers.json.VillagerTradesHandler;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VillagerTradesResourceListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public VillagerTradesResourceListener() {
        //Fortnite
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "villager_trades");
    }

    @Override
    public Identifier getFabricId() {
        return NumismaticOverhaul.id("villager_data_loader");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        if (!NumismaticOverhaul.CONFIG.enableVillagerTrading()) return;

        NumismaticVillagerTradesRegistry.clearRegistries();

        loader.forEach((identifier, jsonElement) -> {
            if (!jsonElement.isJsonObject()) return;
            JsonObject root = jsonElement.getAsJsonObject();
            if(!HammersAndTablesCompat(identifier)) VillagerTradesHandler.loadProfession(identifier, root);
        });

        NumismaticVillagerTradesRegistry.wrapModVillagers();

        final Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> registry = NumismaticVillagerTradesRegistry.getRegistryForLoading();
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.putAll(registry.getLeft());

        if (!registry.getRight().isEmpty()) {
            TradeOffers.WANDERING_TRADER_TRADES.clear();
            TradeOffers.WANDERING_TRADER_TRADES.putAll(registry.getRight());
        }

    }

    //Trade offers from json files for armorer, toolsmith and weaponsmith are disabled when Hammers and Smithing or Frycmod is installed
    public boolean HammersAndTablesCompat(Identifier file){
        if(FabricLoader.getInstance().isModLoaded("hammersandtables")){
            return Objects.equals(file.toString(), "numismatic-overhaul:armorer") ||
                    Objects.equals(file.toString(), "numismatic-overhaul:toolsmith") ||
                    Objects.equals(file.toString(), "numismatic-overhaul:weaponsmith");
        }
        else if(FabricLoader.getInstance().isModLoaded("frycmod")){
            return Objects.equals(file.toString(), "numismatic-overhaul:armorer") ||
                    Objects.equals(file.toString(), "numismatic-overhaul:toolsmith") ||
                    Objects.equals(file.toString(), "numismatic-overhaul:weaponsmith") ||
                    Objects.equals(file.toString(), "numismatic-overhaul:librarian");
        }
        return false;
    }
}
