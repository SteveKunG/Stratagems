package com.stevekung.stratagems.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class StratagemPod extends Entity
{
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
    protected void defineSynchedData(Builder builder)
    {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {

    }
}