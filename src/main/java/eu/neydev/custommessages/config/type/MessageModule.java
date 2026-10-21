package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;

/**
 * Модули сообщений о событиях игрока.
 *
 * Каждый модуль хранит ключ секции в config.yml: в 2.0 модули живут в
 * modules.&lt;key&gt;, в 1.x секция была верхнеуровневой (join, quit, death) -
 * старый путь читается с единственной подсказкой LEGACY_PATH на модуль.
 */
public enum MessageModule {

    JOIN("join"),
    QUIT("quit"),
    DEATH("death");

    private final String configKey;

    MessageModule(String configKey) {
        this.configKey = configKey;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

}
