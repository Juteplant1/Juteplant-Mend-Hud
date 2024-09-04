package net.juteplant;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.List;

public class HudRenderingShit implements HudRenderCallback {

    private static final int DURA_PER_BOTTLE = 14;
    private static final int MAX_TARGET_DURABILITY = 407;

    private boolean wasHoldingXpBottle = false;
    private int targetDurability = 0;

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;

        boolean isHoldingXpBottle = mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE
                || mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE;

        if (isHoldingXpBottle && !wasHoldingXpBottle) {
            ItemStack[] armorItems = {
                    mc.player.getInventory().armor.get(3), // Helmet
                    mc.player.getInventory().armor.get(2), // Chestplate
                    mc.player.getInventory().armor.get(1), // Leggings
                    mc.player.getInventory().armor.get(0)  // Boots
            };

            targetDurability = calculate_target_durability(armorItems, get_experience_from_bottles(mc));
        }

        wasHoldingXpBottle = isHoldingXpBottle;

        if (isHoldingXpBottle) {
            int x = mc.getWindow().getScaledWidth() - 100;
            int y = mc.getWindow().getScaledHeight() - 100;
            int shadowOffset = 1;

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            String text = String.format("Target Mend: %d", targetDurability);

            drawContext.drawText(textRenderer, text, x + shadowOffset, y + shadowOffset, 0xFF000000, false); // Shadow in black
            drawContext.drawText(textRenderer, text, x, y, 0xFFFFFF, false); // White text without shadow
        }
    }

    private static int get_experience_from_bottles(MinecraftClient mc) {
        int xpBottleCount = 0;

        for (ItemStack stack : mc.player.getInventory().main) {
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) {
                xpBottleCount += stack.getCount();
            }
        }

        ItemStack offHandStack = mc.player.getInventory().offHand.getFirst();
        if (offHandStack.getItem() == Items.EXPERIENCE_BOTTLE) {
            xpBottleCount += offHandStack.getCount();
        }

        return xpBottleCount;
    }

    private static int calculate_target_durability(ItemStack[] armorItems, int experience) {
        List<ItemStack> armor = Arrays.stream(armorItems).toList();
        int averagedura = getAveragedura(armor);
        int exp = (experience * DURA_PER_BOTTLE) / 4;

        if(averagedura + exp <= MAX_TARGET_DURABILITY) {
            return averagedura + exp;
        }
        else {
            return MAX_TARGET_DURABILITY;
        }
    }

    private static int getAveragedura(List<ItemStack> armor) {
        int totalDurability = 0;
        int itemCount = 0;

        for (ItemStack item : armor) {
            if (!item.isEmpty()) {
                totalDurability += item.getMaxDamage() - item.getDamage();
                itemCount++;
            }
        }

        return itemCount > 0 ? totalDurability / itemCount : 0;
    }

}
