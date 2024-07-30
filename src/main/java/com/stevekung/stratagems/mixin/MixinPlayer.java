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
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class MixinPlayer implements PlayerStratagemsAccessor
{
    @Unique
    private final Map<Holder<Stratagem>, StratagemInstance> stratagems = Maps.newHashMap();

    @Override
    public Map<Holder<Stratagem>, StratagemInstance> getPlayerStratagems()
    {
        return this.stratagems;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addStratagemSaveData(CompoundTag compound, CallbackInfo info)
    {
        if (!this.stratagems.isEmpty())
        {
            var listTag = new ListTag();

//            for (MobEffectInstance mobEffectInstance : this.activeEffects.values())
//            {
//                listTag.add(mobEffectInstance.save());
//            }

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
//                CompoundTag compoundTag = listTag.getCompound(i);
//                MobEffectInstance mobEffectInstance = MobEffectInstance.load(compoundTag);
//                if (mobEffectInstance != null)
//                {
//                    this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
//                }
            }
        }
    }
}