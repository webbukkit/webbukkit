package org.webbukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.webbukkit.http.HttpHandler;
import org.webbukkit.http.server.HttpServer;

public class WebbukkitPlugin extends JavaPlugin {

    protected static final Logger log = Logger.getLogger("Minecraft");

    HttpServer httpServer;
    HttpPathDelegator httpHandler = new HttpPathDelegator();
    HashMap<Plugin, PluginRegistration> pluginRegistrations = new HashMap<Plugin, PluginRegistration>();
    HashMap<HttpHandler, PluginRegistration> handlerRegistrations = new HashMap<HttpHandler, PluginRegistration>();
    
    public WebbukkitPlugin() {
    }

    public void registerHandler(Plugin plugin, String path, HttpHandler handler) {
        httpHandler.registerHandler(path, handler);
        
        PluginRegistration registration = pluginRegistrations.get(plugin);
        
        if (registration == null) {
            registration = new PluginRegistration(plugin);
            pluginRegistrations.put(plugin, registration);
        }
        handlerRegistrations.put(handler, registration);
        registration.handlers.add(handler);
    }
    
    public void unregisterHandler(HttpHandler handler) {
        httpHandler.unregisterHandler(handler);
        PluginRegistration registration = handlerRegistrations.remove(handler);
        if (registration != null) {
            registration.handlers.remove(handler);
            if (registration.handlers.isEmpty()) {
                pluginRegistrations.remove(registration.plugin);
            }
        }
    }
    
    public void unregisterHandlers(Plugin plugin) {
        PluginRegistration registration = pluginRegistrations.remove(plugin);
        if (registration != null) {
            for(HttpHandler handler : registration.handlers) {
                httpHandler.unregisterHandler(handler);
                handlerRegistrations.remove(handler);
            }
        }
    }
    
    @Override
    public void onEnable() {
        String bindAddressString = getConfiguration().getString("bindaddress", "0.0.0.0");
        InetAddress bindAddress = null;
        try {
            bindAddress = bindAddressString.equals("0.0.0.0")
                    ? null
                    : InetAddress.getByName(bindAddressString);
        } catch (UnknownHostException e) {
        }
        int port = getConfiguration().getInt("port", 80);
        httpServer = new HttpServer(bindAddress, port, httpHandler);
        try {
            httpServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        registerEvents();
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.shutdown();
            httpServer = null;
        }
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, new ServerListener() {
            @Override
            public void onPluginDisable(PluginDisableEvent event) {
                unregisterHandlers(event.getPlugin());
            }
        }, Priority.Monitor, this);
    }
    
    private static class PluginRegistration {
        public Plugin plugin;
        public Set<HttpHandler> handlers = new HashSet<HttpHandler>();
        
        public PluginRegistration(Plugin plugin) {
            this.plugin = plugin;
        }
    }
}
