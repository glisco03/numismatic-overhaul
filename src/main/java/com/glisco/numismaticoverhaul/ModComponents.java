package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<CurrencyComponent> CURRENCY = ComponentRegistry.getOrCreate(Identifier.of("numismatic-overhaul", "currency"), CurrencyComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(CURRENCY, CurrencyComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
