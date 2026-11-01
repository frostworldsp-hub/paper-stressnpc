package com.example.stressnpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FakePlayerSession {

    private final StressNPCPlugin plugin;
    private final String name;
    private final UUID uuid;
    private final Location spawnLocation;

    private final MinecraftServer server;
    private final WorldServer world;

    private EntityPlayer fakePlayer;
    private BukkitTask patrolTask;
    private BukkitTask randomMoveTask;
    private int routeIndex = 0;
    private boolean movementEnabled = true;

    private final List<Location> route = new ArrayList<>();
    private int autoHotbarSlot = 0;
    private int autoMenuSlot = 13;
    private boolean autoRightClick = true;
    private String autoServerName = null;
    private String autoDonateGroup = null;

    public FakePlayerSession(StressNPCPlugin plugin, String name, UUID uuid, Location spawnLocation) {
        this.plugin = plugin;
        this.name = name;
        this.uuid = uuid;
        this.spawnLocation = spawnLocation.clone();

        this.server = ((CraftServer) Bukkit.getServer()).getServer();
        this.world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
    }

    public String getName() {
        return name;
    }

    public String getLowerName() {
        return name.toLowerCase(Locale.ROOT);
    }

    public EntityPlayer getFakePlayer() {
        return fakePlayer;
    }

    public boolean isMovementEnabled() {
        return movementEnabled;
    }

    public void setMovementEnabled(boolean enabled) {
        this.movementEnabled = enabled;
        if (enabled) {
            startPatrol();
            startRandomMove();
        } else {
            if (patrolTask != null) {
                patrolTask.cancel();
            }
            if (randomMoveTask != null) {
                randomMoveTask.cancel();
            }
        }
    }

    public void resumeMovement() {
        this.movementEnabled = true;
        startPatrol();
        startRandomMove();
    }

    public void stopMovement() {
        this.movementEnabled = false;
        if (patrolTask != null) {
            patrolTask.cancel();
        }
        if (randomMoveTask != null) {
            randomMoveTask.cancel();
        }
    }

    public void setAutoLobbyFlow(int hotbarSlot, int menuSlot, boolean rightClick, String serverName, String donateGroup) {
        this.autoHotbarSlot = hotbarSlot;
        this.autoMenuSlot = menuSlot;
        this.autoRightClick = rightClick;
        this.autoServerName = serverName;
        this.autoDonateGroup = donateGroup;
    }

    public void spawn() {
        GameProfile profile = new GameProfile(uuid, name);
        NetworkManager networkManager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);
        PlayerInteractManager interactManager = new PlayerInteractManager(world);

        this.fakePlayer = new EntityPlayer(server, world, profile, interactManager);
        this.fakePlayer.playerConnection = new PlayerConnection(server, networkManager, fakePlayer);

        this.fakePlayer.setLocation(
                spawnLocation.getX(),
                spawnLocation.getY(),
                spawnLocation.getZ(),
                spawnLocation.getYaw(),
                spawnLocation.getPitch()
        );

        this.server.getPlayerList().a(networkManager, fakePlayer);

        buildRoute();
        startPatrol();
        startRandomMove();
        triggerAuthAndFlow();
    }

    public void triggerAuthAndFlow() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fakePlayer == null) {
                    return;
                }

                fakePlayer.chat("/register password123");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (fakePlayer == null) {
                            return;
                        }

                        fakePlayer.chat("/login password123");

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (fakePlayer == null) {
                                    return;
                                }
                                performLobbyFlow(autoHotbarSlot, autoMenuSlot, autoRightClick, autoServerName, autoDonateGroup);
                            }
                        }.runTaskLater(plugin, 25L);
                    }
                }.runTaskLater(plugin, 18L);
            }
        }.runTaskLater(plugin, 1L);
    }

    public void setHeldSlot(int slotId) {
        if (fakePlayer == null || fakePlayer.playerConnection == null) {
            return;
        }
        if (slotId < 0 || slotId > 8) {
            return;
        }

        fakePlayer.inventory.itemInHandIndex = slotId;
        fakePlayer.playerConnection.a(new PacketPlayInHeldItemSlot(slotId));
    }

    public void leftClick() {
        if (fakePlayer == null || fakePlayer.playerConnection == null) {
            return;
        }
        fakePlayer.playerConnection.a(new PacketPlayInArmAnimation(EnumHand.MAIN_HAND));
    }

    public void rightClick() {
        if (fakePlayer == null || fakePlayer.playerConnection == null) {
            return;
        }
        fakePlayer.playerConnection.a(new PacketPlayInUseItem(EnumHand.MAIN_HAND));
    }

    public void guiClick(int slotId, boolean rightClick) {
        if (fakePlayer == null || fakePlayer.playerConnection == null) {
            return;
        }

        if (fakePlayer.activeContainer == null) {
            return;
        }

        int windowId = fakePlayer.activeContainer.windowId;
        if (windowId < 0) {
            return;
        }

        int button = rightClick ? 1 : 0;
        fakePlayer.playerConnection.a(new PacketPlayInWindowClick(
                windowId,
                slotId,
                button,
                InventoryClickType.PICKUP,
                ItemStack.EMPTY,
                (short) 0
        ));
    }

    public void performLobbyFlow(int hotbarSlot, int menuSlot, boolean rightClick, String serverName, String donateGroup) {
        if (fakePlayer == null || fakePlayer.playerConnection == null) {
            return;
        }

        setHeldSlot(hotbarSlot);
        rightClick();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (fakePlayer == null || fakePlayer.playerConnection == null) {
                    return;
                }

                guiClick(menuSlot, rightClick);

                if (serverName != null && !serverName.trim().isEmpty()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "send " + name + " " + serverName);
                            if (donateGroup != null && !donateGroup.trim().isEmpty()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + name + " parent add " + donateGroup);
                            }
                        }
                    }.runTaskLater(plugin, 20L);
                } else if (donateGroup != null && !donateGroup.trim().isEmpty()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + name + " parent add " + donateGroup);
                        }
                    }.runTaskLater(plugin, 20L);
                }
            }
        }.runTaskLater(plugin, 8L);
    }

    private void buildRoute() {
        route.clear();
        double x = spawnLocation.getX();
        double y = spawnLocation.getY();
        double z = spawnLocation.getZ();

        route.add(spawnLocation.clone());
        route.add(new Location(spawnLocation.getWorld(), x + 1.5D, y, z));
        route.add(new Location(spawnLocation.getWorld(), x + 3.0D, y, z + 1.5D));
        route.add(new Location(spawnLocation.getWorld(), x + 4.5D, y, z));
        route.add(new Location(spawnLocation.getWorld(), x + 3.0D, y, z - 1.5D));
        route.add(new Location(spawnLocation.getWorld(), x + 1.5D, y, z - 3.0D));
        route.add(new Location(spawnLocation.getWorld(), x, y, z));
    }

    public void startPatrol() {
        if (!movementEnabled) {
            return;
        }

        if (patrolTask != null) {
            patrolTask.cancel();
        }

        patrolTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!movementEnabled || fakePlayer == null) {
                    return;
                }

                if (route.isEmpty()) {
                    return;
                }

                Location target = route.get(routeIndex % route.size());
                routeIndex++;

                fakePlayer.setLocation(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        target.getYaw(),
                        target.getPitch()
                );

                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(fakePlayer);
                fakePlayer.playerConnection.sendPacket(teleportPacket);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void startRandomMove() {
        if (!movementEnabled) {
            return;
        }

        if (randomMoveTask != null) {
            randomMoveTask.cancel();
        }

        randomMoveTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!movementEnabled || fakePlayer == null) {
                    return;
                }

                double radius = 4.0D;
                double dx = (Math.random() - 0.5D) * radius * 2.0D;
                double dz = (Math.random() - 0.5D) * radius * 2.0D;
                double x = fakePlayer.locX() + dx;
                double y = fakePlayer.locY();
                double z = fakePlayer.locZ() + dz;

                fakePlayer.setLocation(x, y, z, 0.0F, 0.0F);
                PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(fakePlayer);
                fakePlayer.playerConnection.sendPacket(teleport);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void destroy() {
        stopMovement();
        if (fakePlayer != null) {
            fakePlayer.playerConnection = null;
            fakePlayer.dead = true;
        }
    }
}
