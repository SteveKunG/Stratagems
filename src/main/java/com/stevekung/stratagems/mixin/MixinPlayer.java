package com.stevekung.stratagems.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.PlayerStratagemsAccessor;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;
import com.stevekung.stratagems.util.StratagemUtils;

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
    private final Map<Holder<Stratagem>, StratagemInstance> stratagems = Maps.newHashMap();

    MixinPlayer()
    {
        super(null, null);
    }

    @Override
    public Map<Holder<Stratagem>, StratagemInstance> getPlayerStratagems()
    {
        return this.stratagems;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addStratagemSaveData(CompoundTag compound, CallbackInfo info)
    {
        if (this.stratagems.isEmpty())//TODO
        {
            var holder = this.registryAccess().lookupOrThrow(ModRegistries.STRATAGEM).getOrThrow(Stratagems.TNT);
            this.stratagems.put(holder, StratagemUtils.createInstanceWithDefaultValue(holder));
        }
        if (!this.stratagems.isEmpty())
        {
            var listTag = new ListTag();

            for (var stratagemInstance : this.stratagems.values())
            {
                listTag.add(stratagemInstance.save());
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
                var stratagemInstance = StratagemInstance.load(compoundTag);

                if (stratagemInstance != null)
                {
                    this.stratagems.put(stratagemInstance.getStratagem(), stratagemInstance);
                }
            }
        }
    }
}