package org.swdc.packager;

import org.swdc.config.annotations.ConfigureSource;
import org.swdc.config.configs.JsonConfigHandler;
import org.swdc.fx.config.ApplicationConfig;

@ConfigureSource(value = "assets/application.json",handler = JsonConfigHandler.class)
public class PackagerConfigure extends ApplicationConfig {



}
