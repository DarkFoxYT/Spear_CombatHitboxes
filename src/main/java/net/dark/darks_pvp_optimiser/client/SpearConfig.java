package net.dark.darks_pvp_optimiser.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SpearConfig {
    static final float MIN_LINE_WIDTH = 0.5f;
    static final float MAX_LINE_WIDTH = 10.0f;
    private static final float DEFAULT_TARGET_WIDTH = 3.0f;
    private static final float DEFAULT_MARGIN_WIDTH = 1.5f;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("spear_combathitboxes.json");
    private static final SpearConfig INSTANCE = load();

    public int spearColor = 0xffff9000;
    public int meleeColor = 0xffff0000;
    public float lineWidth = DEFAULT_TARGET_WIDTH;
    public boolean accurateHitbox = false;
    public int marginColor = 0xa0ff9000;
    public float marginLineWidth = DEFAULT_MARGIN_WIDTH;

    public static SpearConfig get() {
        return INSTANCE;
    }

    private static SpearConfig load() {
        if (!Files.exists(FILE)) {
            return new SpearConfig();
        }
        try (var reader = Files.newBufferedReader(FILE)) {
            SpearConfig config = GSON.fromJson(reader, SpearConfig.class);
            if (config == null) {
                return new SpearConfig();
            }
            config.lineWidth = validWidth(config.lineWidth, DEFAULT_TARGET_WIDTH);
            config.marginLineWidth = validWidth(config.marginLineWidth, DEFAULT_MARGIN_WIDTH);
            return config;
        } catch (IOException | RuntimeException exception) {
            LoggerFactory.getLogger("Spear+CombatHitboxes").warn("Could not load spear settings", exception);
            return new SpearConfig();
        }
    }

    private static float validWidth(float value, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return Math.clamp(value, MIN_LINE_WIDTH, MAX_LINE_WIDTH);
    }

    public void save() {
        Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(temporary, GSON.toJson(this));
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save spear settings", exception);
        }
    }
}
