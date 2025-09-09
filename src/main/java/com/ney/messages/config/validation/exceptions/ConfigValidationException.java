package com.ney.messages.config.validation.exceptions;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ConfigValidationException extends InvalidConfigException {

    private final List<String> errors;

    public ConfigValidationException(@NotNull List<String> errors) {
        super("Configuration validation failed with " + errors.size() + " errors");
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<String> getErrors() {
        return errors;
    }

}