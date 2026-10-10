package io.github.thebusybiscuit.slimefun4.core.services.sounds;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import be.seeseemelk.mockbukkit.MockBukkit;

/**
 * This class tests the {@link SoundService}, {@link SoundEffect} and {@link SoundConfiguration}.
 * 
 * @see SoundService
 *
 */
class TestSoundService {

    private static Slimefun plugin;

    @BeforeAll
    static void load() {
        MockBukkit.mock();
        plugin = MockBukkit.load(Slimefun.class);
    }

    @AfterAll
    static void unload() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test that every SoundEffect has a valid SoundConfiguration")
    void testEverySoundHasConfiguration() {
        SoundService service = Slimefun.getSoundService();

        for (SoundEffect sound : SoundEffect.values()) {
            SoundConfiguration config = service.getConfiguration(sound);

            Assertions.assertNotNull(config, () -> "Missing configuration for " + sound);
            Assertions.assertNotNull(config.getSoundId());
            Assertions.assertTrue(config.getVolume() >= 0, () -> "Invalid volume for " + sound);
            Assertions.assertTrue(config.getPitch() >= 0.5F, () -> "Invalid pitch for " + sound);
        }
    }

    @Test
    @DisplayName("Test that the default values are loaded when no sounds.yml overrides them")
    void testDefaultValues() {
        SoundService service = Slimefun.getSoundService();

        for (SoundEffect sound : SoundEffect.values()) {
            SoundConfiguration config = service.getConfiguration(sound);

            Assertions.assertNotNull(config);
            Assertions.assertEquals(sound.getDefaultSoundId(), config.getSoundId());
            Assertions.assertEquals(sound.getDefaultVolume(), config.getVolume());
            Assertions.assertEquals(sound.getDefaultPitch(), config.getPitch());
        }
    }

    @Test
    @DisplayName("Test that an invalid volume and pitch are clamped to their minimum values")
    void testInvalidValuesAreClamped() {
        SoundService service = Slimefun.getSoundService();
        SoundEffect sound = SoundEffect.values()[0];

        Config config = new Config(plugin, "sounds.yml");

        try {
            config.setValue(sound.name() + ".volume", -10.0F);
            config.setValue(sound.name() + ".pitch", 0.0F);
            config.save();

            service.reload(false);

            SoundConfiguration configuration = service.getConfiguration(sound);

            Assertions.assertNotNull(configuration);
            Assertions.assertEquals(0, configuration.getVolume());
            Assertions.assertEquals(0.5F, configuration.getPitch());
        } finally {
            // Restore the defaults so other tests are not affected
            config.setValue(sound.name() + ".volume", sound.getDefaultVolume());
            config.setValue(sound.name() + ".pitch", sound.getDefaultPitch());
            config.save();

            service.reload(false);
        }
    }

    @Test
    @DisplayName("Test that a SoundConfiguration returns the values it was created with")
    void testSoundConfiguration() {
        SoundConfiguration config = new SoundConfiguration("minecraft:custom_sound", 0.25F, 1.5F);

        Assertions.assertEquals("minecraft:custom_sound", config.getSoundId());
        Assertions.assertEquals(0.25F, config.getVolume());
        Assertions.assertEquals(1.5F, config.getPitch());
    }

    @Test
    @DisplayName("Test that getConfiguration() rejects null")
    void testGetConfigurationRejectsNull() {
        SoundService service = Slimefun.getSoundService();
        Assertions.assertThrows(NullPointerException.class, () -> service.getConfiguration(null));
    }
}
