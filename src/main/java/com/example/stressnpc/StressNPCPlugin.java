package com.example.stressnpc;

import org.bukkit.plugin.java.JavaPlugin;

public final class StressNPCPlugin extends JavaPlugin {

    private StressNPCManager manager;

    @Override
    public void onEnable() {
        this.manager = new StressNPCManager(this);

        if (getCommand("stressnpc") != null) {
            getCommand("stressnpc").setExecutor(new StressNPCCommand(this, manager));
        } else {
            getLogger().severe("Command 'stressnpc' not found in plugin.yml");
        }
    }

    @Override
    public void onDisable() {
        if (this.manager != null) {
            this.manager.shutdown();
        }
    }
}
