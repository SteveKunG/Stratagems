package com.stevekung.stratagems.entity;

import java.util.Optional;

import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.registry.ModEntityDataSerializers;
import com.stevekung.stratagems.registry.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.level.Level;

public class StratagemPod extends Entity implements VariantHolder<Holder<Stratagem>>
{
    private static final EntityDataAccessor<Holder<Stratagem>> DATA_STRATAGEM = SynchedEntityData.defineId(StratagemPod.class, ModEntityDataSerializers.STRATAGEM);

    public StratagemPod(EntityType<? extends StratagemPod> entityType, Level level)
    {
        super(entityType, level);
        this.noCulling = true;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.tickCount > 200)
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        var registryAccess = this.registryAccess();
        var registry = registryAccess.registryOrThrow(ModRegistries.STRATAGEM);
        builder.define(DATA_STRATAGEM, registry.getHolder(Stratagems.REINFORCE).or(registry::getAny).orElseThrow());
    }

    @Override
    public Holder<Stratagem> getVariant()
    {
        return this.entityData.get(DATA_STRATAGEM);
    }

    @Override
    public void setVariant(Holder<Stratagem> variant)
    {
        this.entityData.set(DATA_STRATAGEM, variant);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound)
    {
        this.getVariant().unwrapKey().ifPresent(resourceKey -> compound.putString("variant", resourceKey.location().toString()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString("variant"))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> this.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).ifPresent(this::setVariant);
    }
}