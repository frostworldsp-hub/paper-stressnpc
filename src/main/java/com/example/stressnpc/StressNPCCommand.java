package com.example.stressnpc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class StressNPCCommand implements CommandExecutor {

    private final StressNPCPlugin plugin;
    private final StressNPCManager manager;

    public StressNPCCommand(StressNPCPlugin plugin, StressNPCManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean allowed = sender.hasPermission("testserver.admin") || (sender instanceof ConsoleCommandSender);
        if (!allowed) {
            sender.sendMessage("§cNo permission: testserver.admin");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "spawn": {
                    int count = Integer.parseInt(args[1]);
                    int hotbarSlot = args.length > 2 ? Integer.parseInt(args[2]) : 0;
                    int menuSlot = args.length > 3 ? Integer.parseInt(args[3]) : 13;
                    boolean rightClick = args.length > 4 ? "right".equalsIgnoreCase(args[4]) : true;
                    String serverName = args.length > 5 ? args[5] : null;
                    String donateGroup = args.length > 6 ? args[6] : null;
                    manager.spawn(count, hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
                    sender.sendMessage("§aSpawned " + count + " bots with auto-lobby flow.");
                    return true;
                }

                case "batch": {
                    int count = Integer.parseInt(args[1]);
                    int delaySeconds = Integer.parseInt(args[2]);
                    int hotbarSlot = Integer.parseInt(args[3]);
                    int menuSlot = Integer.parseInt(args[4]);
                    boolean rightClick = args.length > 5 ? "right".equalsIgnoreCase(args[5]) : true;
                    String serverName = args.length > 6 ? args[6] : null;
                    String donateGroup = args.length > 7 ? args[7] : null;
                    manager.spawnBatch(count, delaySeconds * 20L, hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
                    sender.sendMessage("§aStarted batch spawn: " + count + " bots, delay " + delaySeconds + " seconds.");
                    return true;
                }

                case "lobby": {
                    if (args.length < 5) {
                        sender.sendMessage("§cUsage: /stressnpc lobby <nick> <hotbarSlot> <menuSlot> <left|right> [serverName] [group]");
                        return true;
                    }
                    String nick = args[1];
                    int hotbarSlot = Integer.parseInt(args[2]);
                    int menuSlot = Integer.parseInt(args[3]);
                    boolean rightClick = "right".equalsIgnoreCase(args[4]);
                    String serverName = args.length > 5 ? args[5] : null;
                    String donateGroup = args.length > 6 ? args[6] : null;
                    manager.lobbyFlow(nick, hotbarSlot, menuSlot, rightClick, serverName, donateGroup);
                    sender.sendMessage("§aExecuted lobby flow for " + nick + ".");
                    return true;
                }

                case "join": {
                    if (args.length < 3) {
                        sender.sendMessage("§cUsage: /stressnpc join <nick> <serverName> [group]");
                        return true;
                    }
                    String nick = args[1];
                    String serverName = args[2];
                    String donateGroup = args.length > 3 ? args[3] : null;
                    manager.joinServer(nick, serverName, donateGroup);
                    sender.sendMessage("§aSent " + nick + " to server " + serverName + ".");
                    return true;
                }

                case "setslot": {
                    if (args.length != 3) {
                        sender.sendMessage("§cUsage: /stressnpc setslot <nick> <0-8>");
                        return true;
                    }
                    manager.setHeldSlot(args[1], Integer.parseInt(args[2]));
                    sender.sendMessage("§aHotbar updated.");
                    return true;
                }

                case "click": {
                    if (args.length != 3) {
                        sender.sendMessage("§cUsage: /stressnpc click <nick> <left|right>");
                        return true;
                    }
                    manager.click(args[1], args[2]);
                    sender.sendMessage("§aPacket click sent.");
                    return true;
                }

                case "gui": {
                    if (args.length != 3) {
                        sender.sendMessage("§cUsage: /stressnpc gui <nick> <slot>");
                        return true;
                    }
                    manager.guiClick(args[1], Integer.parseInt(args[2]), false);
                    sender.sendMessage("§aGUI click sent.");
                    return true;
                }

                case "grant": {
                    if (args.length != 3) {
                        sender.sendMessage("§cUsage: /stressnpc grant <nick> <group>");
                        return true;
                    }
                    String nick = args[1];
                    String group = args[2];
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + nick + " parent add " + group);
                    sender.sendMessage("§aGranted group " + group + " to " + nick + ".");
                    return true;
                }

                case "stopall": {
                    manager.stopAll();
                    sender.sendMessage("§aStopped movement for all bots.");
                    return true;
                }

                case "stop": {
                    if (args.length != 2) {
                        sender.sendMessage("§cUsage: /stressnpc stop <nick>");
                        return true;
                    }
                    manager.stop(args[1]);
                    sender.sendMessage("§aStopped movement for " + args[1] + ".");
                    return true;
                }

                case "resumeall": {
                    manager.resumeAll();
                    sender.sendMessage("§aResumed movement for all bots.");
                    return true;
                }

                case "resume": {
                    if (args.length != 2) {
                        sender.sendMessage("§cUsage: /stressnpc resume <nick>");
                        return true;
                    }
                    manager.resume(args[1]);
                    sender.sendMessage("§aResumed movement for " + args[1] + ".");
                    return true;
                }

                default:
                    sendUsage(sender);
                    return true;
            }
        } catch (Exception ex) {
            sender.sendMessage("§cError: " + ex.getMessage());
            ex.printStackTrace();
            return true;
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§7Usage:");
        sender.sendMessage("§7/stressnpc spawn <count> [hotbarSlot] [menuSlot] [left|right] [serverName] [group]");
        sender.sendMessage("§7/stressnpc batch <count> <delaySeconds> <hotbarSlot> <menuSlot> <left|right> [serverName] [group]");
        sender.sendMessage("§7/stressnpc lobby <nick> <hotbarSlot> <menuSlot> <left|right> [serverName] [group]");
        sender.sendMessage("§7/stressnpc join <nick> <serverName> [group]");
        sender.sendMessage("§7/stressnpc setslot <nick> <0-8>");
        sender.sendMessage("§7/stressnpc click <nick> <left|right>");
        sender.sendMessage("§7/stressnpc gui <nick> <slot>");
        sender.sendMessage("§7/stressnpc grant <nick> <group>");
        sender.sendMessage("§7/stressnpc stop <nick>");
        sender.sendMessage("§7/stressnpc stopall");
        sender.sendMessage("§7/stressnpc resume <nick>");
        sender.sendMessage("§7/stressnpc resumeall");
    }
}
