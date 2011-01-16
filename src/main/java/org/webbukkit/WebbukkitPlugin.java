package org.webbukkit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;
import org.webbukkit.NanoHTTPD;

public class WebbukkitPlugin extends JavaPlugin {
	
	protected static final Logger log = Logger.getLogger("Minecraft");

	private NanoHTTPD server = null;
	
	public WebbukkitPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	public void onEnable() {
		try {
			server = new NanoHTTPD(81);
		} catch(IOException e) {
			log.info("position failed to start WebServer (IOException)");
		}
	}

	public void onDisable() {
		if(server != null) {
			server.stop();
			server = null;
		}
	}
}
