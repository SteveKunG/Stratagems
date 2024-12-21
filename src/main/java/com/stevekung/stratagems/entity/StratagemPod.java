package com.stevekung.stratagems.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.references.ModEntityDataSerializers;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.level.Level;

public class StratagemPod extends Entity implements VariantHolder<Holder<Stratagem>>
{
    private static final EntityDataAccessor<Holder<Stratagem>> DATA_STRATAGEM = SynchedEntityData.defineId(StratagemPod.class, ModEntityDataSerializers.STRATAGEM);

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private int inboundTick;

    public StratagemPod(EntityType<? extends StratagemPod> entityType, Level level)
    {
        super(entityType, level);
        this.noCulling = true;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.inboundTick > 0)
        {
            this.inboundTick--;
        }

        if (!this.level().isClientSide() && this.inboundTick == 0 && this.getOwner() instanceof ServerPlayer serverPlayer)
        {
            var holder = this.getVariant();
            var stratagemContext = new StratagemActionContext(serverPlayer, (ServerLevel) this.level(), this.blockPosition(), this.random);
            holder.value().action().action(stratagemContext);
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
        this.getVariant().unwrapKey().ifPresent(resourceKey -> compound.putString(ModConstants.Tag.VARIANT, resourceKey.location().toString()));

        if (this.ownerUUID != null)
        {
            compound.putUUID("Owner", this.ownerUUID);
        }
        compound.putInt("InboundTick", this.getInboundTick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString(ModConstants.Tag.VARIANT))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> this.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).ifPresent(this::setVariant);

        if (compound.hasUUID("Owner"))
        {
            this.ownerUUID = compound.getUUID("Owner");
            this.cachedOwner = null;
        }
        if (compound.contains("InboundTick"))
        {
            this.setInboundTick(compound.getInt("InboundTick"));
        }
    }

    public void setOwner(@Nullable final Entity owner)
    {
        if (owner != null)
        {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public Entity getOwner()
    {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved())
        {
            return this.cachedOwner;
        }
        if (this.ownerUUID != null)
        {
            var level = this.level();

            if (level instanceof ServerLevel serverLevel)
            {
                return this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
            }
        }
        return null;
    }

    public int getInboundTick()
    {
        return this.inboundTick;
    }

    public void setInboundTick(int inboundTick)
    {
        this.inboundTick = inboundTick;
    }
}