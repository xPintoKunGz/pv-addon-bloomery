package bloomery.pvaddon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.event.render.VoiceDistanceRenderEvent;
import su.plo.voice.api.event.EventSubscribe;

@Addon(
        id = "pv-addon-bloomery",
        name = "Bloomery Plasmovoice Addon",
        version = "1.0.0",
        authors = {"Bloomery Network Team"}
)
public final class BPVAddon implements AddonInitializer {

    private static final long HOLD_MS = 600L;
    private static final long FADE_MS = 1500L;
    private static final long TOTAL_MS = HOLD_MS + FADE_MS;
    private static final int SEGMENTS = 128;
    private static final float RING_THICKNESS = 1f;
    private static final int COLOR = 0xFF00FF55;

    // ===== State =====
    @InjectPlasmoVoice
    private PlasmoVoiceClient voiceClient;

    private volatile int currentRadius = 0;
    private volatile long lastEventTime = 0;
    private volatile boolean hasLoggedRender = false;

    // ===== Lifecycle =====
    @Override
    public void onAddonInitialize() {
        voiceClient.getEventBus().register(this, this);

        WorldRenderEvents.LAST.register(this::onWorldRender);

        System.out.println("[BPVAddon] Initialized. Registered WorldRenderEvents.LAST");
    }

    @Override
    public void onAddonShutdown() {
        if (voiceClient != null) {
            voiceClient.getEventBus().unregister(this);
        }
        System.out.println("[BPVAddon] Shutdown");
    }

    // ===== Event handler =====
    @EventSubscribe
    public void onVoiceDistanceRender(VoiceDistanceRenderEvent event) {
        event.setCancelled(true);
        int r = event.getRadius();
        this.currentRadius = r;
        this.lastEventTime = System.currentTimeMillis();
        this.hasLoggedRender = false;
        System.out.println("[BPVAddon-DEBUG] VoiceDistanceRenderEvent received, radius=" + r);
    }

    // ===== World Render =====
    private void onWorldRender(WorldRenderContext ctx) {
        int radius = currentRadius;
        long eventTime = lastEventTime;

        if (radius <= 0 || eventTime == 0) return;

        long elapsed = System.currentTimeMillis() - eventTime;
        if (elapsed < 0 || elapsed >= TOTAL_MS) return;

        float fade;
        if (elapsed < HOLD_MS) {
            fade = 1.0f;
        } else {
            fade = 1.0f - ((elapsed - HOLD_MS) / (float) FADE_MS);
        }
        if (fade <= 0.01f) return;

        if (!hasLoggedRender) {
            System.out.println("[BPVAddon-DEBUG] WorldRender fired, radius=" + radius
                    + ", fade=" + fade);
            hasLoggedRender = true;
        }

        renderRing(ctx, radius, fade);
    }

    // ===== Drawing =====
    private void renderRing(WorldRenderContext ctx, int radius, float fade) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // คำนวณพิกัดแบบ Camera-relative
        Camera camera = ctx.camera();
        Vec3d camPos = camera.getPos();
        double cx = mc.player.getX() - camPos.x;
        double cy = (mc.player.getY() + 0.05) - camPos.y;
        double cz = mc.player.getZ() - camPos.z;

        float a = ((COLOR >> 24) & 0xFF) / 255f * fade;
        float r = ((COLOR >> 16) & 0xFF) / 255f;
        float g = ((COLOR >> 8) & 0xFF) / 255f;
        float b = (COLOR & 0xFF) / 255f;

        float inner = radius - RING_THICKNESS / 2f;
        float outer = radius + RING_THICKNESS / 2f;

        // ตั้งค่า OpenGL
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        // วาดวงแหวนด้วย TRIANGLE_STRIP
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        var mat = ctx.matrixStack().peek().getPositionMatrix();
        for (int i = 0; i <= SEGMENTS; i++) {
            double ang = 2 * Math.PI * i / SEGMENTS;
            double cos = Math.cos(ang);
            double sin = Math.sin(ang);

            // ขอบนอก
            buf.vertex(mat,
                            (float) (cx + cos * outer),
                            (float) cy,
                            (float) (cz + sin * outer))
                    .color(r, g, b, a).next();

            // ขอบใน
            buf.vertex(mat,
                            (float) (cx + cos * inner),
                            (float) cy,
                            (float) (cz + sin * inner))
                    .color(r, g, b, a).next();
        }
        tess.draw();

        // คืนค่า OpenGL state
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}