package com.example.stressnpc;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class StressNPCManager {

    private final StressNPCPlugin plugin;
    private final Map<String, FakePlayerSession> sessions = new ConcurrentHashMap<>();

    public StressNPCManager(StressNPCPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawn(int count) {
        spawn(count, 0, 13, true, null, null);
    }

    public void spawn(int count, int hotbarSlot, int menuSlot, boolean rightClick, String serverName, String donateGroup) {
        if (count <= 0) {
            return;
        }

        World world = Bukkit.getWorlds().get(0);
        if (world == null) {
            return;
        }

        Location base = world.getSpawnLocation().clone();
        Set<String> usedNames = new HashSet<>();

        for (int i = 0; i < count; i++) {
            final int index = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    String nick = NameGenerator.generateUnique(usedNames);
                    usedNames.add(nick.toLowerCase(Locale.ROOT));

                    UUID uuid = UUID.nameUUIDFromBytes(
                            ("stressnpc:" + nick + ":" + System.nanoTime() + ":" + index)
                                    .getBytes(StandardCharsets.UTF_8)
                    );

                    Location spawnLoc = base.clone().add(index * 2.5D, 0.0D, (index % 2 == 0 ? 1.5D : -1.5D));
                    FakePlayerSession session = new FakePlayerSession(plugin, nick, uuid, spawnLoc);
                    session.setAutoLobbyFlow(hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
                    session.spawn();
                    sessions.put(nick.toLowerCase(Locale.ROOT), session);
                }
            }.runTaskLater(plugin, index * 20L);
        }
    }

    public void spawnBatch(int count, long delayTicks, int hotbarSlot, int menuSlot, boolean rightClick, String serverName, String donateGroup) {
        if (count <= 0) {
            return;
        }

        World world = Bukkit.getWorlds().get(0);
        if (world == null) {
            return;
        }

        Location base = world.getSpawnLocation().clone();
        Set<String> usedNames = new HashSet<>();

        for (int i = 0; i < count; i++) {
            final int index = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    String nick = NameGenerator.generateUnique(usedNames);
                    usedNames.add(nick.toLowerCase(Locale.ROOT));

                    UUID uuid = UUID.nameUUIDFromBytes(
                            ("stressnpc:" + nick + ":" + System.nanoTime() + ":" + index)
                                    .getBytes(StandardCharsets.UTF_8)
                    );

                    Location spawnLoc = base.clone().add(index * 2.5D, 0.0D, (index % 2 == 0 ? 1.5D : -1.5D));
                    FakePlayerSession session = new FakePlayerSession(plugin, nick, uuid, spawnLoc);
                    session.setAutoLobbyFlow(hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
                    session.spawn();
                    sessions.put(nick.toLowerCase(Locale.ROOT), session);
                }
            }.runTaskLater(plugin, delayTicks * index);
        }
    }

    public int size() {
        return sessions.size();
    }

    public void stopAll() {
        for (FakePlayerSession session : sessions.values()) {
            if (session != null) {
                session.stopMovement();
            }
        }
    }

    public void resumeAll() {
        for (FakePlayerSession session : sessions.values()) {
            if (session != null) {
                session.resumeMovement();
            }
        }
    }

    public void stop(String nick) {
        FakePlayerSession session = getSession(nick);
        if (session != null) {
            session.stopMovement();
        }
    }

    public void resume(String nick) {
        FakePlayerSession session = getSession(nick);
        if (session != null) {
            session.resumeMovement();
        }
    }

    public void joinServer(String nick, String serverName, String donateGroup) {
        FakePlayerSession session = getSession(nick);
        if (session == null) {
            return;
        }

        session.setAutoLobbyFlow(0, 13, true, serverName, donateGroup);
        session.performLobbyFlow(0, 13, true, serverName, donateGroup);
    }

    public FakePlayerSession getSession(String nick) {
        if (nick == null) {
            return null;
        }
        return sessions.get(nick.toLowerCase(Locale.ROOT));
    }

    public void setHeldSlot(String nick, int slotId) {
        FakePlayerSession session = getSession(nick);
        if (session != null) {
            session.setHeldSlot(slotId);
        }
    }

    public void click(String nick, String side) {
        FakePlayerSession session = getSession(nick);
        if (session == null) {
            return;
        }

        if ("left".equalsIgnoreCase(side)) {
            session.leftClick();
        } else if ("right".equalsIgnoreCase(side)) {
            session.rightClick();
        }
    }

    public void guiClick(String nick, int slotId, boolean rightClick) {
        FakePlayerSession session = getSession(nick);
        if (session == null) {
            return;
        }
        session.guiClick(slotId, rightClick);
    }

    public void lobbyFlow(String nick, int hotbarSlot, int menuSlot, boolean rightClick, String serverName, String donateGroup) {
        FakePlayerSession session = getSession(nick);
        if (session == null) {
            return;
        }
        session.setAutoLobbyFlow(hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
        session.performLobbyFlow(hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
    }

    public void shutdown() {
        for (FakePlayerSession session : sessions.values()) {
            if (session != null) {
                session.destroy();
            }
        }
        sessions.clear();
    }
}
