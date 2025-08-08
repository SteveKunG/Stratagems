package com.stevekung.stratagems.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.references.ModEntityDataSerializers;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.registry.Stratagems;

import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StratagemPod extends Entity
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
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount)
    {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(DATA_STRATAGEM, VariantUtils.getDefaultOrAny(this.registryAccess(), Stratagems.REINFORCE));
    }

    public Holder<Stratagem> getVariant()
    {
        return this.entityData.get(DATA_STRATAGEM);
    }

    public void setVariant(Holder<Stratagem> variant)
    {
        this.entityData.set(DATA_STRATAGEM, variant);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueOutput)
    {
        VariantUtils.writeVariant(valueOutput, this.getVariant());
        valueOutput.storeNullable("Owner", UUIDUtil.CODEC, this.ownerUUID);
        valueOutput.putInt("InboundTick", this.getInboundTick());
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueInput)
    {
        VariantUtils.readVariant(valueInput, ModRegistries.STRATAGEM).ifPresent(this::setVariant);
        this.ownerUUID = valueInput.read("Owner", UUIDUtil.CODEC).orElse(null);
        this.cachedOwner = null;
        this.setInboundTick(valueInput.getIntOr("InboundTick", 0));
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