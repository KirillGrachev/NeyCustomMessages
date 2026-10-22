package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
            List.of("commands.ncm.permission", "commands.ncm.subcommands.reload.permission"));

    private final String configKey;
    private final String defaultPermission;

    /**
     * List.of в аргументах конструктора неизменяем, но анализатор судит по
     * объявленному типу - отсюда точечное подавление вместо замены типа.
     */
    @SuppressWarnings("ImmutableEnumChecker")
    private final List<String> legacyPaths;

    PermissionNode(String configKey, String defaultPermission, List<String> legacyPaths) {
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
     * Пути узла в конфигурации 1.x, пустой список для узлов, появившихся в 2.0.
     */
    public @NotNull List<String> getLegacyPaths() {
        return legacyPaths;
    }

}
