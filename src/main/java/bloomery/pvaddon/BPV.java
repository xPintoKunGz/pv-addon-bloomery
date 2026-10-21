package bloomery.pvaddon;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

public class BPV implements ModInitializer {
	public static final String MOD_ID = "bpvaddon";

	@Override
	public void onInitialize() {
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
