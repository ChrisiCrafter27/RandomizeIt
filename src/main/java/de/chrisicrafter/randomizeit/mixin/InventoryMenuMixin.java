package de.chrisicrafter.randomizeit.mixin;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingContainer> {
    @Unique private final MenuType<?> randomizeIt$menuType;
    @Unique private int randomizeIt$quickcraftType = -1;
    @Unique private int randomizeIt$quickcraftStatus;
    @Unique private final Set<Slot> randomizeIt$quickcraftSlots = Sets.newHashSet();

    public InventoryMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
        this.randomizeIt$menuType = menuType;
    }

    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack quickMoveStack(Slot instance, Player player, int slot) {
        return randomizeIt$getRandomized(instance, player, slot);
    }

    @Unique
    private ItemStack randomizeIt$getRandomized(Slot instance, Player player, int slot) {
        return randomizeIt$getRandomized(instance.getItem(), player, slot);
    }

    @Unique ItemStack randomizeIt$getRandomized(ItemStack item, Player player, int slot) {
        if(player.level() instanceof ServerLevel level && level.getGameRules().getBoolean(ModGameRules.RANDOM_CRAFTING_RESULT) && slot == 0 && !item.is(Items.AIR))
            return new ItemStack(RandomizerData.getInstance(level, player).getRandomizedItemForRecipe(item.getItem(), true), item.getCount());
        else return item;
    }

    @Override
    public void clicked(int slotId, int buttonId, @NotNull ClickType clickType, Player player) {
        if(!player.level().getGameRules().getBoolean(ModGameRules.RANDOM_CRAFTING_RESULT) && slotId == 0) {
            super.clicked(slotId, buttonId, clickType, player);
            return;
        }
        try {
            this.randomizeIt$doClick(slotId, buttonId, clickType, player);
        } catch (Exception exception) {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
            crashreportcategory.setDetail("Menu Type", () -> this.randomizeIt$menuType != null ? BuiltInRegistries.MENU.getKey(this.randomizeIt$menuType).toString() : "<no type>");
            crashreportcategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashreportcategory.setDetail("Slot Count", this.slots.size());
            crashreportcategory.setDetail("Slot", slotId);
            crashreportcategory.setDetail("Button", buttonId);
            crashreportcategory.setDetail("Type", clickType);
            throw new ReportedException(crashreport);
        }
    }

    @Unique
    public void randomizeIt$doClick(int slotId, int buttonId, ClickType clickType, Player player) {
        Inventory inventory = player.getInventory();
        if (clickType == ClickType.QUICK_CRAFT) {
            int i = this.randomizeIt$quickcraftStatus;
            this.randomizeIt$quickcraftStatus = getQuickcraftHeader(buttonId);
            if ((i != 1 || this.randomizeIt$quickcraftStatus != 2) && i != this.randomizeIt$quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.randomizeIt$quickcraftStatus == 0) {
                this.randomizeIt$quickcraftType = getQuickcraftType(buttonId);
                if (isValidQuickcraftType(this.randomizeIt$quickcraftType, player)) {
                    this.randomizeIt$quickcraftStatus = 1;
                    this.randomizeIt$quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.randomizeIt$quickcraftStatus == 1) {
                Slot slot = this.slots.get(slotId);
                ItemStack itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && (this.randomizeIt$quickcraftType == 2 || itemstack.getCount() > this.randomizeIt$quickcraftSlots.size()) && this.canDragTo(slot)) {
                    this.randomizeIt$quickcraftSlots.add(slot);
                }
            } else if (this.randomizeIt$quickcraftStatus == 2) {
                if (!this.randomizeIt$quickcraftSlots.isEmpty()) {
                    if (this.randomizeIt$quickcraftSlots.size() == 1) {
                        int i1 = (this.randomizeIt$quickcraftSlots.iterator().next()).index;
                        this.resetQuickCraft();
                        this.randomizeIt$doClick(i1, this.randomizeIt$quickcraftType, ClickType.PICKUP, player);
                        return;
                    }

                    ItemStack itemstack2 = this.getCarried().copy();
                    if (itemstack2.isEmpty()) {
                        this.resetQuickCraft();
                        return;
                    }

                    int k1 = this.getCarried().getCount();

                    for(Slot slot1 : this.randomizeIt$quickcraftSlots) {
                        ItemStack itemstack1 = this.getCarried();
                        if (slot1 != null && canItemQuickReplace(slot1, itemstack1, true) && slot1.mayPlace(itemstack1) && (this.randomizeIt$quickcraftType == 2 || itemstack1.getCount() >= this.randomizeIt$quickcraftSlots.size()) && this.canDragTo(slot1)) {
                            int j = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            int k = Math.min(itemstack2.getMaxStackSize(), slot1.getMaxStackSize(itemstack2));
                            int l = Math.min(getQuickCraftPlaceCount(this.randomizeIt$quickcraftSlots, this.randomizeIt$quickcraftType, itemstack2) + j, k);
                            k1 -= l - j;
                            slot1.setByPlayer(itemstack2.copyWithCount(l));
                        }
                    }

                    itemstack2.setCount(k1);
                    this.setCarried(itemstack2);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.randomizeIt$quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (buttonId == 0 || buttonId == 1)) {
            ClickAction clickaction = buttonId == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (clickaction == ClickAction.PRIMARY) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }

                Slot slot6 = this.slots.get(slotId);
                if (!slot6.mayPickup(player)) {
                    return;
                }

                for(ItemStack itemstack8 = this.quickMoveStack(player, slotId); !itemstack8.isEmpty() && ItemStack.isSameItem(randomizeIt$getRandomized(slot6, player, slotId), itemstack8); itemstack8 = this.quickMoveStack(player, slotId)) {}
            } else {
                if (slotId < 0) {
                    return;
                }

                Slot slot7 = this.slots.get(slotId);
                ItemStack itemstack9 = randomizeIt$getRandomized(slot7, player, slotId);
                ItemStack itemstack10 = this.getCarried();

                player.updateTutorialInventoryAction(itemstack10, randomizeIt$getRandomized(slot7, player, slotId), clickaction);
                if (!this.randomizeIt$tryItemClickBehaviourOverride(player, clickaction, slot7, itemstack9, itemstack10)) {
                    if (!net.minecraftforge.common.ForgeHooks.onItemStackedOn(itemstack9, itemstack10, slot7, clickaction, player, randomizeIt$createCarriedSlotAccess()))
                        if (itemstack9.isEmpty()) {
                            if (!itemstack10.isEmpty()) {
                                int i3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                this.setCarried(randomizeIt$getRandomized(slot7.safeInsert(itemstack10, i3), player, slotId));
                            }
                        } else if (slot7.mayPickup(player)) {
                            if (itemstack10.isEmpty()) {
                                int j3 = clickaction == ClickAction.PRIMARY ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                                Optional<ItemStack> optional1 = slot7.tryRemove(j3, Integer.MAX_VALUE, player);
                                optional1.ifPresent((item) -> {
                                    this.setCarried(randomizeIt$getRandomized(item, player, slotId));
                                    slot7.onTake(player, item);
                                });
                            } else if (slot7.mayPlace(itemstack10)) {
                                if (ItemStack.isSameItemSameTags(itemstack9, itemstack10)) {
                                    int k3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                    this.setCarried(randomizeIt$getRandomized(slot7.safeInsert(itemstack10, k3), player, slotId));
                                } else if (itemstack10.getCount() <= slot7.getMaxStackSize(itemstack10)) {
                                    this.setCarried(randomizeIt$getRandomized(itemstack9, player, slotId));
                                    slot7.setByPlayer(itemstack10);
                                }
                            } else if (ItemStack.isSameItemSameTags(itemstack9, itemstack10)) {
                                Optional<ItemStack> optional = slot7.tryRemove(itemstack9.getCount(), itemstack10.getMaxStackSize() - itemstack10.getCount(), player);
                                optional.ifPresent((p_150428_) -> {
                                    itemstack10.grow(p_150428_.getCount());
                                    slot7.onTake(player, p_150428_);
                                });
                            }
                        }
                }

                slot7.setChanged();
            }
        } else if (clickType == ClickType.SWAP) {
            Slot slot2 = this.slots.get(slotId);
            ItemStack itemstack3 = inventory.getItem(buttonId);
            ItemStack itemstack6 = randomizeIt$getRandomized(slot2, player, slotId);
            if (!itemstack3.isEmpty() || !itemstack6.isEmpty()) {
                if (itemstack3.isEmpty()) {
                    if (slot2.mayPickup(player)) {
                        try {
                            inventory.setItem(buttonId, itemstack6);

                            Class<? extends Slot> clazz = slot2.getClass();
                            Method method = clazz.getMethod("onSwapCraft", Integer.class);
                            method.setAccessible(true);
                            method.invoke(slot2, itemstack6.getCount());
                            method.setAccessible(false);

                            slot2.setByPlayer(ItemStack.EMPTY);
                            slot2.onTake(player, itemstack6);
                        } catch (Exception e) {
                            LogUtils.getLogger().warn("Exception caught during item swap in CraftingMenuMixin: " + Arrays.toString(e.getStackTrace()));
                        }
                    }
                } else if (itemstack6.isEmpty()) {
                    if (slot2.mayPlace(itemstack3)) {
                        int i2 = slot2.getMaxStackSize(itemstack3);
                        if (itemstack3.getCount() > i2) {
                            slot2.setByPlayer(itemstack3.split(i2));
                        } else {
                            inventory.setItem(buttonId, ItemStack.EMPTY);
                            slot2.setByPlayer(itemstack3);
                        }
                    }
                } else if (slot2.mayPickup(player) && slot2.mayPlace(itemstack3)) {
                    int j2 = slot2.getMaxStackSize(itemstack3);
                    if (itemstack3.getCount() > j2) {
                        slot2.setByPlayer(itemstack3.split(j2));
                        slot2.onTake(player, itemstack6);
                        if (!inventory.add(itemstack6)) {
                            player.drop(itemstack6, true);
                        }
                    } else {
                        inventory.setItem(buttonId, itemstack6);
                        slot2.setByPlayer(itemstack3);
                        slot2.onTake(player, itemstack6);
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot5 = this.slots.get(slotId);
            if (slot5.hasItem()) {
                ItemStack itemstack5 = randomizeIt$getRandomized(slot5, player, slotId);
                this.setCarried(itemstack5.copyWithCount(itemstack5.getMaxStackSize()));
            }
        } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot4 = this.slots.get(slotId);
            int j1 = buttonId == 0 ? 1 : slot4.getItem().getCount();
            ItemStack itemstack7 = slot4.safeTake(j1, Integer.MAX_VALUE, player);
            player.drop(itemstack7, true);
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot3 = this.slots.get(slotId);
            ItemStack itemstack4 = this.getCarried();
            if (!itemstack4.isEmpty() && (!slot3.hasItem() || !slot3.mayPickup(player))) {
                int l1 = buttonId == 0 ? 0 : this.slots.size() - 1;
                int k2 = buttonId == 0 ? 1 : -1;

                for(int l2 = 0; l2 < 2; ++l2) {
                    for(int l3 = l1; l3 >= 0 && l3 < this.slots.size() && itemstack4.getCount() < itemstack4.getMaxStackSize(); l3 += k2) {
                        Slot slot8 = this.slots.get(l3);
                        if (slot8.hasItem() && canItemQuickReplace(slot8, itemstack4, true) && slot8.mayPickup(player) && this.canTakeItemForPickAll(itemstack4, slot8)) {
                            ItemStack itemstack11 = randomizeIt$getRandomized(slot8, player, slotId);
                            if (l2 != 0 || itemstack11.getCount() != itemstack11.getMaxStackSize()) {
                                ItemStack itemstack12 = slot8.safeTake(itemstack11.getCount(), itemstack4.getMaxStackSize() - itemstack4.getCount(), player);
                                itemstack4.grow(itemstack12.getCount());
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean randomizeIt$tryItemClickBehaviourOverride(Player p_249615_, ClickAction p_250300_, Slot p_249384_, ItemStack p_251073_, ItemStack p_252026_) {
        FeatureFlagSet featureflagset = p_249615_.level().enabledFeatures();
        if (p_252026_.isItemEnabled(featureflagset) && p_252026_.overrideStackedOnOther(p_249384_, p_250300_, p_249615_)) {
            return true;
        } else {
            return p_251073_.isItemEnabled(featureflagset) && p_251073_.overrideOtherStackedOnMe(p_252026_, p_249384_, p_250300_, p_249615_, this.randomizeIt$createCarriedSlotAccess());
        }
    }

    @Unique
    private SlotAccess randomizeIt$createCarriedSlotAccess() {
        return new SlotAccess() {
            public ItemStack get() {
                return ((CraftingMenu) (Object) this).getCarried();
            }

            public boolean set(@NotNull ItemStack stack) {
                ((CraftingMenu) (Object) this).setCarried(stack);
                return true;
            }
        };
    }

    @Unique
    protected void resetQuickCraft() {
        this.randomizeIt$quickcraftStatus = 0;
        this.randomizeIt$quickcraftSlots.clear();
    }
}
