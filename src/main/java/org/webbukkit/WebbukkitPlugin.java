package org.webbukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.webbukkit.http.server.HttpServer;

public class WebbukkitPlugin extends JavaPlugin {

    protected static final Logger log = Logger.getLogger("Minecraft");

    HttpServer httpServer;
    HttpPathDelegator httpHandler = new HttpPathDelegator();

    public WebbukkitPlugin() {
    }

    @Override
    public void onLoad() {
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
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.shutdown();
            httpServer = null;
        }
    }
}
