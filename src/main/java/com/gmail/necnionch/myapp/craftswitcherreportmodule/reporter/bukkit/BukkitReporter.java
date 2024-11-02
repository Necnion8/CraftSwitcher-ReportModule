package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.CraftSwitcherReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.IReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands.SwitcherCommand;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands.SwitcherRestartCommand;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands.SwitcherStartCommand;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands.SwitcherStopCommand;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.TCPClient;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BukkitReporter implements IReporter, TCPClient.OnClientListener {
    private static BukkitReporter instance;
    private CraftSwitcherReporter craft;
    private String nmsVersion;
    private Class<?> craftServerClass;
    private Object nmsServer;
    private Field recentTpsField;
    private boolean initializedServer;


    public BukkitReporter(CraftSwitcherReporter rep) {
        instance = this;
        Bukkit.getLogger().info("Loading CraftSwitcherReporter by Necnion8");
        craft = rep;

        craft.getSocket().addListener(this);
        craft.changeState(ServerState.STARTING);

        nmsVersion = getNMSVersion();
        craftServerClass = getCraftClass("CraftServer");
        Class<?> nmsServerClass = getNMSClass("MinecraftServer");

        if (nmsServerClass != null) {
            try {
                Method getMethod = nmsServerClass.getDeclaredMethod("getServer");
                nmsServer = getMethod.invoke(null);
                recentTpsField = nmsServerClass.getDeclaredField("recentTps");
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        registerCommands();
    }

    public static void init() {
        CraftSwitcherReporter rep = CraftSwitcherReporter.getInstance();
        if (rep == null)
            return;

        try {
            rep.setReporter(new BukkitReporter(rep));

        } catch (Exception e) {
            Bukkit.getLogger().severe("CraftSwitcherReporter loading fail!");
            e.printStackTrace();
        }
    }

    public static void onSetDefaultCommands() {
        if (instance != null)
            instance.registerCommands();
    }

    public static void onInitServer() {
        if (instance != null) {
            instance.initializedServer = true;
            instance.craft.changeState(ServerState.STARTED);

        }
    }



    public void registerCommands() {
        SimpleCommandMap commandMap;
        try {
            commandMap = (SimpleCommandMap) craftServerClass
                    .getDeclaredMethod("getCommandMap")
                    .invoke(Bukkit.getServer());

            commandMap.register("switcher", new SwitcherCommand());
            commandMap.register("switcher", new SwitcherStartCommand());
            commandMap.register("switcher", new SwitcherStopCommand());
            commandMap.register("switcher", new SwitcherRestartCommand());

        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to register CraftSwitcher commands.");
        }
    }


    public static String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    public Class<?> getCraftClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + nmsVersion + "." + name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + nmsVersion + "." + name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }



    public void onConnectClient() {
        craft.changeState((initializedServer) ? ServerState.STARTED : ServerState.STARTING);
    }

    public void onDisconnectClient() {

    }

    public SerializableData onReceiveData(SerializableData data, int dataId) {
        return null;
    }





    @Override
    public String getReporterName() {
        return "Bukkit";
    }

    @Override
    public Map<UUID, String> getOnlinePlayers() {
        Map<UUID, String> players = new HashMap<>();

        Bukkit.getOnlinePlayers().forEach(p ->
                players.put(p.getUniqueId(), p.getName())
        );
        return players;
    }

    @Override
    public Integer getMaxPlayer() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public Double getTps() {
        if (recentTpsField != null) {
            try {
                return ((double[]) recentTpsField.get(nmsServer))[0];

            } catch (IllegalAccessException | ClassCastException ignored) {
            }
        }
        return null;
    }

    @Override
    public Boolean isStarted() {
        return initializedServer;
    }
}
