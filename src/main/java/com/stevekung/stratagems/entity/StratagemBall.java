package com.stevekung.stratagems.entity;

import java.util.Optional;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.ModEntityDataSerializers;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.references.StratagemSounds;
import com.stevekung.stratagems.api.util.PacketUtils;
import com.stevekung.stratagems.registry.ModEntities;
import com.stevekung.stratagems.registry.Stratagems;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class StratagemBall extends ThrowableItemProjectile implements VariantHolder<Holder<Stratagem>>
{
    private static final EntityDataAccessor<Holder<Stratagem>> DATA_STRATAGEM = SynchedEntityData.defineId(StratagemBall.class, ModEntityDataSerializers.STRATAGEM);
    private static final EntityDataAccessor<StratagemInstance.Side> DATA_STRATAGEM_SIDE = SynchedEntityData.defineId(StratagemBall.class, ModEntityDataSerializers.STRATAGEM_SIDE);

    public StratagemBall(EntityType<? extends StratagemBall> entityType, Level level)
    {
        super(entityType, level);
    }

    public StratagemBall(Level level, LivingEntity shooter)
    {
        super(ModEntities.STRATAGEM_BALL, shooter, level);
    }

    public StratagemBall(Level level, double x, double y, double z)
    {
        super(ModEntities.STRATAGEM_BALL, x, y, z, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        var registry = this.registryAccess().registryOrThrow(ModRegistries.STRATAGEM);
        builder.define(DATA_STRATAGEM, registry.getHolder(Stratagems.REINFORCE).or(registry::getAny).orElseThrow());
        builder.define(DATA_STRATAGEM_SIDE, StratagemInstance.Side.SERVER);
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

    public StratagemInstance.Side getSide()
    {
        return this.entityData.get(DATA_STRATAGEM_SIDE);
    }

    public void setSide(StratagemInstance.Side state)
    {
        this.entityData.set(DATA_STRATAGEM_SIDE, state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        this.getVariant().unwrapKey().ifPresent(resourceKey -> compound.putString(ModConstants.Tag.VARIANT, resourceKey.location().toString()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString(ModConstants.Tag.VARIANT))).map(resourceLocation -> ResourceKey.create(ModRegistries.STRATAGEM, resourceLocation)).flatMap(resourceKey -> this.registryAccess().registryOrThrow(ModRegistries.STRATAGEM).getHolder(resourceKey)).ifPresent(this::setVariant);
    }

    @Override
    protected Item getDefaultItem()
    {
        return Items.SNOWBALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);
        //        var entity = result.getEntity();
        //        var i = entity instanceof Blaze ? 3 : 0;
        //        entity.hurt(this.damageSources().thrown(this, this.getOwner()), i);
    }

    @Override
    protected void onHit(HitResult result)
    {
        super.onHit(result);

        if (this.level() instanceof ServerLevel serverLevel)
        {
            var holder = this.getVariant();
            var stratagemPod = new StratagemPod(ModEntities.STRATAGEM_POD, this.level());
            stratagemPod.setVariant(holder);
            stratagemPod.setOwner(this.getOwner());
            stratagemPod.moveTo(this.blockPosition(), 0.0f, 0.0f);

            if (this.getOwner() instanceof ServerPlayer serverPlayer)
            {
                var stratagemsData = this.getSide() == StratagemInstance.Side.SERVER ? serverLevel.getServer().overworld().stratagemsData() : serverPlayer.stratagemsData();

                if (stratagemsData.canUse(holder, serverPlayer))
                {
                    stratagemsData.use(holder, serverPlayer);
                    stratagemPod.setInboundTick(stratagemsData.instanceByHolder(holder).inboundDuration);

                    if (this.getSide() == StratagemInstance.Side.SERVER)
                    {
                        PacketUtils.sendClientUpdatePacketS2P(this.getServer(), UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));
                    }
                    else
                    {
                        PacketUtils.sendClientUpdatePacket2P(serverPlayer, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(holder));
                    }
                }
                else
                {
                    var instance = stratagemsData.instanceByHolder(holder);
                    ModConstants.LOGGER.info("{}", Component.translatable("commands.stratagem.use.failed", instance.stratagem().name(), instance.state.getTranslationName()).getString());
                }
            }
            else
            {
                ModConstants.LOGGER.warn("Stratagem owner is {} rather than a player!", this.getOwner());
            }

            this.level().addFreshEntity(stratagemPod);

            this.playSound(StratagemSounds.STRATAGEM_LAND, 1f, 1.0f);
            this.discard();
        }
    }
}