package net.juteplant;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HudRenderingShit implements HudRenderCallback {

    private static final int XP_PER_BOTTLE = 10;
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
            int totalExperience = getTotalExperienceFromBottles(mc);

            ItemStack[] armorItems = {
                    mc.player.getInventory().armor.get(3), // Helmet
                    mc.player.getInventory().armor.get(2), // Chestplate
                    mc.player.getInventory().armor.get(1), // Leggings
                    mc.player.getInventory().armor.get(0)  // Boots
            };

            targetDurability = calculateTargetDurability(armorItems, totalExperience);
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

    private int getTotalExperienceFromBottles(MinecraftClient mc) {
        int xpBottleCount = 0;

        // Count all XP bottles in the player's inventory
        for (ItemStack stack : mc.player.getInventory().main) {
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) {
                xpBottleCount += stack.getCount();
            }
        }

        return xpBottleCount * XP_PER_BOTTLE;
    }

    private static int calculateTargetDurability(ItemStack[] armorItems, int totalExperience) {
        int low = 0;
        int high = MAX_TARGET_DURABILITY;
        int bestDurability = low;

        while (low <= high) {
            int mid = (low + high) / 2;
            int totalExperienceNeeded = calculateTotalExperience(armorItems, mid);

            if (totalExperienceNeeded <= totalExperience) {
                bestDurability = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return bestDurability;
    }

    private static int calculateTotalExperience(ItemStack[] armorItems, int targetDura) {
        int totalExperienceNeeded = 0;
        for (ItemStack armor : armorItems) {
            if (!armor.isEmpty()) {
                int currentDamage = armor.getDamage();
                int maxDurability = armor.getMaxDamage();
                int remainingDurability = maxDurability - currentDamage;

                int experienceNeeded = (targetDura - remainingDurability);

                if (experienceNeeded > 0) {
                    totalExperienceNeeded += experienceNeeded;
                }
            }
        }
        return totalExperienceNeeded;
    }
}
