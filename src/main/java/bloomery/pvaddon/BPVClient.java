package bloomery.pvaddon;

import net.fabricmc.api.ClientModInitializer;
import su.plo.voice.api.client.PlasmoVoiceClient;

public class BPVClient implements ClientModInitializer {

    private final BPVAddon addon = new BPVAddon();

    @Override
    public void onInitializeClient() {
        // สำคัญมาก: ต้อง load ผ่าน PlasmoVoiceClient
        PlasmoVoiceClient.getAddonsLoader().load(addon);
        System.out.println("[BPV] Plasmo Voice addon loaded");
    }
}