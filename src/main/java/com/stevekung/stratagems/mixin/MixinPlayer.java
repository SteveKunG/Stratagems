package com.stevekung.stratagems.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.accessor.PlayerStratagemsAccessor;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity implements PlayerStratagemsAccessor
{
    @Unique
    private final Map<Holder<Stratagem>, StratagemInstance> stratagems = Maps.newLinkedHashMap();

    MixinPlayer()
    {
        super(null, null);
    }

    @Override
    public Map<Holder<Stratagem>, StratagemInstance> getStratagems()
    {
        return this.stratagems;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo info)
    {
        for (var entry : this.stratagems.entrySet())
        {
            entry.getValue().tick(this.getServer(), Player.class.cast(this));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addStratagemSaveData(CompoundTag compound, CallbackInfo info)
    {
        if (!this.stratagems.isEmpty())
        {
            var listTag = new ListTag();

            for (var instances : this.stratagems.values())
            {
                var compoundTag = new CompoundTag();
                instances.save(compoundTag);
                listTag.add(compoundTag);
            }

            compound.put(ModConstants.Tag.STRATAGEMS, listTag);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(CompoundTag compound, CallbackInfo info)
    {
        if (compound.contains(ModConstants.Tag.STRATAGEMS, Tag.TAG_LIST))
        {
            var listTag = compound.getList(ModConstants.Tag.STRATAGEMS, Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++)
            {
                var compoundTag = listTag.getCompound(i);
                var instance = StratagemInstance.load(compoundTag, this.level());
                this.stratagems.put(instance.getStratagem(), instance);
            }
        }
    }
}