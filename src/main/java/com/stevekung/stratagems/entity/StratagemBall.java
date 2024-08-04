package com.stevekung.stratagems.entity;

import java.util.Optional;

import com.stevekung.stratagems.ModConstants;
import com.stevekung.stratagems.Stratagem;
import com.stevekung.stratagems.StratagemInstance;
import com.stevekung.stratagems.StratagemsMod;
import com.stevekung.stratagems.action.StratagemActionContext;
import com.stevekung.stratagems.packet.UpdatePlayerStratagemsPacket;
import com.stevekung.stratagems.packet.UpdateServerStratagemsPacket;
import com.stevekung.stratagems.registry.*;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
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
            var stratagemPod = new StratagemPod(ModEntities.STRATAGEM_POD, this.level());
            stratagemPod.setVariant(this.getVariant());
            stratagemPod.moveTo(this.blockPosition(), 0.0f, 0.0f);
            this.level().addFreshEntity(stratagemPod);

            if (this.getOwner() instanceof ServerPlayer serverPlayer)
            {
                var stratagemContext = new StratagemActionContext(serverPlayer, serverLevel, this.blockPosition(), this.random);
                var serverStratagems = serverLevel.getServer().overworld().getStratagemData();
                var playerStratagems = serverPlayer.getStratagems().get(this.getVariant());

                if (this.getSide() == StratagemInstance.Side.SERVER)
                {
                    this.getVariant().value().action().action(stratagemContext);
                    serverStratagems.use(this.getVariant(), serverPlayer);
                    
                    for (var player : PlayerLookup.all(this.getServer()))
                    {
                        ServerPlayNetworking.send(player, UpdateServerStratagemsPacket.create(serverStratagems.getInstances()));
                    }
                }
                else
                {
                    if (playerStratagems == null)
                    {
                        return;
                    }

                    this.getVariant().value().action().action(stratagemContext);
                    playerStratagems.use(getServer(), serverPlayer);
                    ServerPlayNetworking.send(serverPlayer, UpdatePlayerStratagemsPacket.create(serverPlayer.getStratagems().values(), serverPlayer.getUUID()));
                }
            }
            else
            {
                StratagemsMod.LOGGER.warn("Stratagem owner is {} rather than a player!", this.getOwner());
            }

            this.playSound(StratagemSounds.STRATAGEM_LAND, 1f, 1.0f);
            this.discard();
        }
    }
}