package com.stevekung.stratagems;

import net.minecraft.world.item.ItemStack;

public record Stratagem(String code, String name, ItemStack itemStack, int initCooldown, int duration, int nextUseCooldown)
{}