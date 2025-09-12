package com.ney.messages.config.validation;

import com.ney.messages.config.*;
import com.ney.messages.config.validation.exceptions.ConfigValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigValidator {

    public void validate(@NotNull MainConfig config) throws ConfigValidationException {

        List<String> errors = new ArrayList<>();

        validateDeathConfig(config.death(), errors);
        validateJoinConfig(config.join(), errors);
        validateQuitConfig(config.quit(), errors);
        validateAnnouncementsConfig(config.announcements(), errors);

        if (!errors.isEmpty()) throw new ConfigValidationException(errors);

    }

    private void validateDeathConfig(@NotNull DeathConfig config, List<String> errors) {
        validateTitleConfig(config.title(), "death.title", errors);
        validateSoundConfig(config.sound(), "death.sound", errors);
        validateBossBarConfig(config.bossBar(), "death.bossBar", errors);
    }

    private void validateJoinConfig(@NotNull JoinConfig config, List<String> errors) {
        validateTitleConfig(config.title(), "join.title", errors);
        validateSoundConfig(config.sound(), "join.sound", errors);
        validateBossBarConfig(config.bossBar(), "join.bossBar", errors);
    }

    private void validateQuitConfig(@NotNull QuitConfig config, List<String> errors) {
        validateTitleConfig(config.title(), "quit.title", errors);
        validateSoundConfig(config.sound(), "quit.sound", errors);
        validateBossBarConfig(config.bossBar(), "quit.bossBar", errors);
    }

    private void validateAnnouncementsConfig(@NotNull AnnouncementsConfig config,
                                             List<String> errors) {

        if (config.enabled() && config.messages().isEmpty()) {
            errors.add("announces.enabled is true but announces.messages is empty");
        }

        if (config.interval() <= 0) {
            errors.add("announces.interval must be greater than 0");
        }

        for (int i = 0; i < config.messages().size(); i++) {
            validateAnnouncementConfig(config.messages().get(i), "announces.messages[" + i + "]", errors);
        }

    }

    private void validateAnnouncementConfig(@NotNull AnnouncementConfig config,
                                            String path, List<String> errors) {
        validateTitleConfig(config.title(), path + ".title", errors);
        validateSoundConfig(config.sound(), path + ".sound", errors);
        validateBossBarConfig(config.bossBar(), path + ".bossBar", errors);
    }

    private void validateTitleConfig(@NotNull TitleConfig config,
                                     String path, List<String> errors) {
        if (config.enabled()) {
            if (config.fadeIn() < 0) errors.add(path + ".fadeIn must be >= 0");
            if (config.stay() < 0) errors.add(path + ".stay must be >= 0");
            if (config.fadeOut() < 0) errors.add(path + ".fadeOut must be >= 0");
        }
    }

    private void validateSoundConfig(@NotNull SoundConfig config,
                                     String path, List<String> errors) {
        if (config.enabled()) {
            if (config.volume() < 0.0f || config.volume() > 2.0f) {
                errors.add(path + ".volume must be between 0.0 and 2.0");
            }
            if (config.pitch() < 0.0f || config.pitch() > 2.0f) {
                errors.add(path + ".pitch must be between 0.0 and 2.0");
            }
        }
    }

    private void validateBossBarConfig(@NotNull BossBarConfig config,
                                       String path, List<String> errors) {

        if (config.enabled()) {

            if (config.duration() <= 0) {
                errors.add(path + ".duration must be greater than 0");
            }

            if (config.color() == null) {
                errors.add(path + ".color is invalid or missing");
            }

            if (config.style() == null) {
                errors.add(path + ".style is invalid or missing");
            }

            if (config.text() == null || config.text().isEmpty()) {
                errors.add(path + ".text is missing or empty");
            }

        }
    }
}