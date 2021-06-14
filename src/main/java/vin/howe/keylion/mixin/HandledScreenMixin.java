package vin.howe.keylion.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vin.howe.keylion.KeyHintHandler;
import vin.howe.keylion.KeybindTooltipRenderer;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow
    @Final
    protected T handler;

    @Shadow
    protected abstract boolean isPointOverSlot(Slot slot, double pointX, double pointY);

    @Shadow
    protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);

    private KeyHintHandler hintHandler = null;

    protected HandledScreenMixin() {
        super(LiteralText.EMPTY);
    }

    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                    shift = At.Shift.AFTER))
    public void drawSlotTooltipOverlay(MatrixStack matrixStack, Slot slot, CallbackInfo ci) {
        if (hintHandler == null) {
            hintHandler = new KeyHintHandler(handler.slots.size(), this.client.options);
        }
        if (!hintHandler.getFilteredHints().containsKey(slot.id) || !hintHandler.getFilteredHints().get(slot.id)) {
            return;
        }

        KeybindTooltipRenderer.renderOverlay(matrixStack, slot, hintHandler.getHintsMap().get(slot.id), slot.x, slot.y, 300);
    }

    @Inject(method = "keyPressed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(II)Z",
                    shift = At.Shift.AFTER))
    public void handleHintKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (hintHandler == null) {
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            hintHandler.clearInput();
            hintHandler.clearSelectedSlot();
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            hintHandler.removeLastChar();
            return;
        }

        InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scanCode);
        String keyText = key.getLocalizedText().getString();
        if (keyText.length() > 1 || KeyHintHandler.HINT_CHARS.indexOf(keyText.charAt(0)) == -1) {
            return;
        }

        Integer lastSelectedSlot = hintHandler.getSelectedSlot();
        hintHandler.pushChar(keyText.charAt(0));

        if (hintHandler.hasSelectedSlot()) {
            int selectedSlot = hintHandler.getSelectedSlot();
            if (lastSelectedSlot != null && selectedSlot != lastSelectedSlot) {
                Slot lastSlot = handler.getSlot(lastSelectedSlot);
                Slot targetSlot = handler.getSlot(selectedSlot);
                ItemStack lastSlotStack = lastSlot.getStack();
                ItemStack targetSlotStack = targetSlot.getStack();
                boolean targetIsSameType = targetSlotStack.isItemEqual(lastSlotStack);
                if (targetSlot.canInsert(lastSlot.getStack()) &&
                        (targetSlotStack.isEmpty() ||
                                (targetIsSameType && targetSlotStack.getCount() < targetSlotStack.getMaxCount())
                        )
                ) {
                    boolean clickBack = targetIsSameType &&
                            targetSlotStack.getMaxCount() - targetSlotStack.getCount() < lastSlotStack.getCount();
                    onMouseClick(handler.getSlot(lastSelectedSlot), lastSelectedSlot, 0, SlotActionType.PICKUP);
                    onMouseClick(handler.getSlot(selectedSlot), selectedSlot, 0, SlotActionType.PICKUP);
                    if (clickBack) {
                        onMouseClick(handler.getSlot(lastSelectedSlot), lastSelectedSlot, 0, SlotActionType.PICKUP);
                    }
                }
                hintHandler.clearSelectedSlot();
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isPointOverSlot(Lnet/minecraft/screen/slot/Slot;DD)Z"))
    public boolean isPointOverSlotRedirect(HandledScreen handledScreen, Slot slot, double pointX, double pointY) {
        if (hintHandler.hasSelectedSlot() && hintHandler.getSelectedSlot() == slot.id) {
            return true;
        }

        return isPointOverSlot(slot, pointX, pointY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return hintHandler.isInputEmpty() && !hintHandler.hasSelectedSlot();
    }
}
