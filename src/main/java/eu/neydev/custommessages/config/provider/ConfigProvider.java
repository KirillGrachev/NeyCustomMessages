package eu.neydev.custommessages.config.provider;

import eu.neydev.custommessages.config.MainConfig;

public interface ConfigProvider {

    MainConfig getConfig();
    void reload() throws Exception;

}