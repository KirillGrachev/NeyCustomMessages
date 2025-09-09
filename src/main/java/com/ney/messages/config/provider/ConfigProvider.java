package com.ney.messages.config.provider;

import com.ney.messages.config.MainConfig;

public interface ConfigProvider {

    MainConfig getConfig();
    void reload() throws Exception;

}