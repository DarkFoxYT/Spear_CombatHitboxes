package net.dark.darks_pvp_optimiser.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SpearSettings {
    private SpearSettings() {}

    public static Screen standaloneScreen(Screen parent) {
        ConfigBuilder screen = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Spear+CombatHitboxes"));
        addCategory(screen);
        return screen.build();
    }

    public static void addCategory(ConfigBuilder screen) {
        SpearConfig settings = SpearConfig.get();
        SpearConfig defaults = new SpearConfig();
        var entries = screen.entryBuilder();
        var spear = screen.getOrCreateCategory(Component.literal("Spear"));

        spear.addEntry(entries.startAlphaColorField(Component.literal("Spear range color"), settings.spearColor)
                .setDefaultValue(defaults.spearColor)
                .setSaveConsumer(color -> settings.spearColor = color)
                .build());
        spear.addEntry(entries.startAlphaColorField(Component.literal("Regular melee range color"), settings.meleeColor)
                .setDefaultValue(defaults.meleeColor)
                .setSaveConsumer(color -> settings.meleeColor = color)
                .build());
        spear.addEntry(entries.startFloatField(Component.literal("Target line width"), settings.lineWidth)
                .setMin(SpearConfig.MIN_LINE_WIDTH)
                .setMax(SpearConfig.MAX_LINE_WIDTH)
                .setDefaultValue(defaults.lineWidth)
                .setSaveConsumer(width -> settings.lineWidth = width)
                .build());

        spear.addEntry(entries.startBooleanToggle(Component.literal("Accurate spear hitbox"), settings.accurateHitbox)
                .setDefaultValue(defaults.accurateHitbox)
                .setTooltip(Component.literal("Show the spear’s extra targeting margin around the white body box."),
                        Component.literal("Uses your held spear, or the first spear in your hotbar."))
                .setSaveConsumer(showMargin -> settings.accurateHitbox = showMargin)
                .build());
        spear.addEntry(entries.startAlphaColorField(Component.literal("Extra hitbox color"), settings.marginColor)
                .setDefaultValue(defaults.marginColor)
                .setTooltip(Component.literal("Color of the extra box on targets outside your crosshair."))
                .setSaveConsumer(color -> settings.marginColor = color)
                .build());
        spear.addEntry(entries.startFloatField(Component.literal("Extra hitbox line width"), settings.marginLineWidth)
                .setMin(SpearConfig.MIN_LINE_WIDTH)
                .setMax(SpearConfig.MAX_LINE_WIDTH)
                .setDefaultValue(defaults.marginLineWidth)
                .setSaveConsumer(width -> settings.marginLineWidth = width)
                .build());

        Runnable saveOtherSettings = screen.getSavingRunnable();
        screen.setSavingRunnable(() -> {
            if (saveOtherSettings != null) {
                saveOtherSettings.run();
            }
            settings.save();
        });
    }
}
