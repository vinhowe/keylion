package vin.howe.keylion.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow
    public int selectedSlot;

    @Shadow
    @Final
    public PlayerEntity player;

    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    private int lastSelectedSlot = -1;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"))
    public void scrollInHotbarHead(double scrollAmount, CallbackInfo ci) {
        lastSelectedSlot = this.selectedSlot;
    }

    @Inject(method = "scrollInHotbar", at = @At("TAIL"))
    public void scrollInHotbarTail(double scrollAmount, CallbackInfo ci) {
        if (Screen.hasAltDown()) {
            ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
            if (interactionManager == null) {
                return;
            }

            if (Screen.hasControlDown()) {
                // TODO: This is hacky but it's late and I don't want to think about it
                if (scrollAmount > 0) {
                    for (int i = 0; i < 8; i++) {
                        interactionManager.clickSlot(0, i + 36, i + 1, SlotActionType.SWAP, player);
                    }
                } else {
                    for (int i = 8; i > 0; i--) {
                        interactionManager.clickSlot(0, i + 36, i - 1, SlotActionType.SWAP, player);
                    }
                }
                this.selectedSlot = lastSelectedSlot;
            } else if (!main.get(lastSelectedSlot).isEmpty()) {
                interactionManager.clickSlot(0, selectedSlot + 36, lastSelectedSlot, SlotActionType.SWAP, player);
            } else {
                ItemStack currentItemStack = main.get(selectedSlot);
                if ((int) scrollAmount == 0) {
                    // Don't think this could ever happen but it would cause an infinite loop
                    return;
                }
                while (currentItemStack.isEmpty()) {
                    selectedSlot = (selectedSlot - (int) scrollAmount) % 9;
                    if (selectedSlot < 0) {
                        selectedSlot = 8;
                    }
                    currentItemStack = main.get(selectedSlot);
                    if (selectedSlot == lastSelectedSlot) {
                        // Didn't work, wrapped all the way around
                        return;
                    }
                }
            }
        }
    }
}
