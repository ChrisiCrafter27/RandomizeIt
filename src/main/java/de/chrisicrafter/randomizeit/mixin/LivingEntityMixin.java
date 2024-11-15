package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected int lastHurtByPlayerTime;

    @Shadow protected abstract boolean shouldDropLoot();
    @Shadow protected abstract void dropFromLootTable(ServerLevel level, DamageSource damageSource, boolean playerKill);
    @Shadow protected abstract void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean playerKill);
    @Shadow protected abstract void dropEquipment(ServerLevel level);
    @Shadow protected abstract void dropExperience(ServerLevel level, Entity entity);

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    public void dropAllDeathLoot(ServerLevel level, DamageSource damageSource, CallbackInfo info) {
        if(level.getGameRules().getBoolean(ModGameRules.RANDOM_MOB_DROPS)) {
            this.dropExperience(level, damageSource.getEntity());

            this.captureDrops(new ArrayList<>());
            boolean flag = this.lastHurtByPlayerTime > 0;
            if (this.shouldDropLoot() && level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.dropFromLootTable(level, damageSource, flag);
                this.dropCustomDeathLoot(level, damageSource, flag);
            }
            Collection<ItemEntity> drops = captureDrops();
            assert drops != null;
            if (!CommonHooks.onLivingDrops((LivingEntity) (Object) this, damageSource, drops, lastHurtByPlayerTime > 0)) {
                drops.forEach(e -> {
                    if(damageSource.getEntity() instanceof ServerPlayer player) {
                        e.setItem(new ItemStack(RandomizerData.getInstance(level.getServer().overworld(), damageSource.getEntity()).getRandomizedItemForMob(e.getItem().getItem(), player, level, true), e.getItem().getCount()));
                        level.addFreshEntity(e);
                    } else if(!level.getGameRules().getBoolean(ModGameRules.PLAYER_UNIQUE_DATA)) {
                        e.setItem(new ItemStack(RandomizerData.getInstance(level.getServer().overworld(), damageSource.getEntity()).getRandomizedItemForMob(e.getItem().getItem(), null, level, true), e.getItem().getCount()));
                        level.addFreshEntity(e);
                    }
                });
            }

            this.captureDrops(new ArrayList<>());
            this.dropEquipment(level);
            Collection<ItemEntity> equipment = captureDrops();
            assert equipment != null;
            if (!CommonHooks.onLivingDrops((LivingEntity) (Object) this, damageSource, drops, lastHurtByPlayerTime > 0)) {
                equipment.forEach(e -> level().addFreshEntity(e));
            }

            info.cancel();
        }
    }
}
