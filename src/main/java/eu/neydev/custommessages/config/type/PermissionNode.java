package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Узлы прав плагина.
 *
 * Узел один - служебная команда доступна только администрации; модули и
 * анонсы работают для всех игроков без прав. {@code legacyPaths} - пути
 * версии 1.x в порядке приоритета: права команды в 1.x задавались в
 * двух местах сразу (commands.ncm.permission и
 * commands.ncm.subcommands.reload.permission), 2.0 читает первое
 * заданное с подсказкой LEGACY_PATH.
 */
public enum PermissionNode {

    RELOAD("reload", "neycustommessages.reload",
            new String[]{"commands.ncm.permission", "commands.ncm.subcommands.reload.permission"});

    private final String configKey;
    private final String defaultPermission;
    private final String[] legacyPaths;

    PermissionNode(String configKey, String defaultPermission, String[] legacyPaths) {
        this.configKey = configKey;
        this.defaultPermission = defaultPermission;
        this.legacyPaths = legacyPaths;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    public @NotNull String getDefaultPermission() {
        return defaultPermission;
    }

    /**
     * Пути узла в конфигурации 1.x, пустые для узлов, появившихся в 2.0.
     */
    public @Nullable String[] getLegacyPaths() {
        return legacyPaths.length == 0 ? null : legacyPaths;
    }

}
