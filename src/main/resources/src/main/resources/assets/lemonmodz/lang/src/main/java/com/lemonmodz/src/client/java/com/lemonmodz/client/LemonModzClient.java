package com.lemonmodz.client;

import com.lemonmodz.LemonModz;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class LemonModzClient implements ClientModInitializer {
	private static boolean enabled = true;

	private static KeyMapping maceKey;

	@Override
	public void onInitializeClient() {
		maceKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.lemonmodz.mace",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath(LemonModz.MOD_ID, "main"))));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
				dispatcher.register(ClientCommands.literal("burfey")
						.then(ClientCommands.literal("on").executes(ctx -> {
							enabled = true;
							ctx.getSource().sendFeedback(Component.literal("LemonModz: ON"));
							return 1;
						}))
						.then(ClientCommands.literal("off").executes(ctx -> {
							enabled = false;
							ctx.getSource().sendFeedback(Component.literal("LemonModz: OFF"));
							return 1;
						}))));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (maceKey.consumeClick()) {
				if (enabled) switchToMace(mc);
			}
		});

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(LemonModz.MOD_ID, "hud"),
				(graphics, deltaTracker) -> {
					if (!enabled) return;
					Minecraft mc = Minecraft.getInstance();
					LocalPlayer player = mc.player;
					if (player == null || mc.options.hideGui) return;

					int x = 4;
					int y = 4;
					int line = 10;
					graphics.drawString(mc.font, "FPS: " + mc.getFps(), x, y, 0xFFFFFF00);
					y += line;
					graphics.drawString(mc.font, String.format("XYZ: %.1f %.1f %.1f",
							player.getX(), player.getY(), player.getZ()), x, y, 0xFFFFFFFF);
					y += line;

					ItemStack held = player.getMainHandItem();
					if (held.is(Items.MACE)) {
						graphics.drawString(mc.font,
								String.format("Mace smash: %.1f dmg", maceDamage(player.fallDistance)),
								x, y, 0xFFFF5555);
					}
				});
	}

	private static void switchToMace(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null) return;
		for (int i = 0; i < 9; i++) {
			if (player.getInventory().getItem(i).is(Items.MACE)) {
				player.getInventory().setSelectedSlot(i);
				return;
			}
		}
	}

	private static double maceDamage(double fall) {
		if (fall <= 1.5) return 6.0;
		double bonus = 4.0 * Math.min(fall, 3.0);
		if (fall > 3.0) bonus += 2.0 * (Math.min(fall, 8.0) - 3.0);
		if (fall > 8.0) bonus += (fall - 8.0);
		return 6.0 + bonus;
	}
            }
