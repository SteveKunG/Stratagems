package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.entity.StratagemBall;
import com.stevekung.stratagems.entity.StratagemPod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities
{
    public static final EntityType<StratagemBall> STRATAGEM_BALL = EntityType.Builder.<StratagemBall>of(StratagemBall::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(10).updateInterval(10).build();
    public static final EntityType<StratagemPod> STRATAGEM_POD = EntityType.Builder.of(StratagemPod::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(10).updateInterval(20).build();

    public static void init()
    {
        register("stratagem_ball", STRATAGEM_BALL);
        register("stratagem_pod", STRATAGEM_POD);
    }

    private static <T extends Entity> void register(String key, EntityType<T> type)
    {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, StratagemsMod.id(key), type);
    }
}