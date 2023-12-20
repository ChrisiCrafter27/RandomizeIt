package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected int lastHurtByPlayerTime;

    @Shadow protected abstract boolean shouldDropLoot();
    @Shadow protected abstract void dropFromLootTable(DamageSource damageSource, boolean playerKill);
    @Shadow protected abstract void dropCustomDeathLoot(DamageSource damageSource, int lootingLevel, boolean playerKill);
    @Shadow protected abstract void dropEquipment();
    @Shadow protected abstract void dropExperience();

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    public void dropAllDeathLoot(DamageSource damageSource, CallbackInfo info) {
        if(level().getGameRules().getBoolean(ModGameRules.RANDOM_MOB_DROPS) && !((LivingEntity) (Object) this instanceof Player)) {
            if(level() instanceof ServerLevel level) {
                Entity entity = damageSource.getEntity();

                int i = net.minecraftforge.common.ForgeHooks.getLootingLevel(this, entity, damageSource);
                this.captureDrops(new java.util.ArrayList<>());

                boolean flag = this.lastHurtByPlayerTime > 0;
                if (this.shouldDropLoot() && level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    this.dropFromLootTable(damageSource, flag);
                    this.dropCustomDeathLoot(damageSource, i, flag);
                }

                this.dropEquipment();
                this.dropExperience();

                Collection<ItemEntity> drops = captureDrops(null);
                if (!net.minecraftforge.common.ForgeHooks.onLivingDrops((LivingEntity) (Object) this, damageSource, drops, i, lastHurtByPlayerTime > 0)) {
                    drops.forEach(e -> {
                        e.setItem(new ItemStack(RandomizerData.getInstance(level.getServer().overworld()).getRandomizedItemForMob(e.getItem().getItem(), true), e.getItem().getCount()));
                        level.addFreshEntity(e);
                    });
                }
            }
            info.cancel();
        }
    }
}
