package com.glisco.numismaticoverhaul.item;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.item.ItemStack;

public interface CurrencyItem {

    KeyedEndec<Long> ORIGINAL_VALUE = new KeyedEndec<>("OriginalValue", Endec.LONG, 0L);

    static void setOriginalValue(ItemStack stack, long value) {
        stack.put(ORIGINAL_VALUE, value);
    }

    static long getOriginalValue(ItemStack stack) {
        return stack.get(ORIGINAL_VALUE);
    }

    static boolean hasOriginalValue(ItemStack stack) {
        return stack.has(ORIGINAL_VALUE);
    }

    boolean wasAdjusted(ItemStack other);

    long getValue(ItemStack stack);

    long[] getCombinedValue(ItemStack stack);

}
