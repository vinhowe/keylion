package vin.howe.keylion;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;

public class KeybindTooltipRenderer {
    public static void renderKeybindTooltip(MatrixStack matrices, Slot slot, String hintText, int x, int y, int z) {
        // TODO: Keep a list of these in HandledScreenMixin
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(hintText);
        DrawableHelper.fill(matrices, x - textWidth - 1, y - 1, x, y + 9, 0xFFFCE300);
        matrices.translate(0, 0, z + 1);
        textRenderer.draw(matrices, hintText, x - textWidth, y, 0xFF212121);
    }

    public static void renderOverlay(MatrixStack matrixStack, Slot slot, String hintText, int x, int y, int z) {
        RenderSystem.enableBlend();
        matrixStack.push();
        matrixStack.translate(x / 2.0, y / 2.0, z);
        matrixStack.scale(1 / 2.0f, 1 / 2.0f, 1);
        matrixStack.translate(16 * 2.0, 0, z);
        renderKeybindTooltip(matrixStack, slot, hintText, x - 1, y - 1, z);
        matrixStack.pop();
        RenderSystem.disableBlend();
    }
}
