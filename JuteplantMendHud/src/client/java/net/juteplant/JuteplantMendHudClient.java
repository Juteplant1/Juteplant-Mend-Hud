package net.juteplant;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class JuteplantMendHudClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(new HudRenderingShit());
	}
}