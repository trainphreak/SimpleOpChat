package com.recon.SimpleOpChat;

import org.bukkit.plugin.java.*;
import java.util.logging.*;
import java.util.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class SimpleOpChat extends JavaPlugin implements Listener
{
    public static final Logger logger;
    private List<String> chatters;

    static {
        logger = Logger.getLogger("Minecraft.SimpleOpChat");
    }

    public SimpleOpChat() {
        this.chatters = new ArrayList<>();
    }

    public void onEnable() {
        this.loadConfig();
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this, this);
        SimpleOpChat.logger.info("[SimpleOpChat] enabled!");
    }

    public void onDisable() {
        SimpleOpChat.logger.info("[SimpleOpChat] disabled!");
    }

    private void loadConfig() {
        this.getConfig().addDefault("prefix", "&f[&eOpChat&f]");
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    private boolean canUseAdminChat(final CommandSender sender) {
        return sender.hasPermission("simpleopchat.readwrite") || sender.isOp() || sender.isOp();
    }

    private String buildMessage(final String[] args, final int start) {
        final StringBuilder msg = new StringBuilder();
        for (int i = start; i < args.length; ++i) {
            if (i != start) {
                msg.append(" ");
            }
            msg.append(args[i]);
        }
        return msg.toString();
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        final String msg = this.buildMessage(args, 0);
        if (commandLabel.equalsIgnoreCase("a")) {
            if (this.canUseAdminChat(sender)) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("toggle")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("[SimpleOpChat] Console can't use the toggle!");
                            return false;
                        }
                        if (this.chatters.contains(sender.getName())) {
                            this.chatters.remove(sender.getName());
                            sender.sendMessage("[SimpleOpChat] Toggle disabled!");
                        }
                        else {
                            this.chatters.add(sender.getName());
                            sender.sendMessage("[SimpleOpChat] Toggle enabled!");
                        }
                        return true;
                    }
                    else {
                        this.sendToChat(msg, sender);
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "[SimpleOpChat] Usage: /a <your message>");
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + "Permission denied!");
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (this.chatters.contains(event.getPlayer().getName())) {
            this.sendToChat(event.getMessage(), event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final String pname = event.getPlayer().getName();
        if (this.chatters.contains(pname)) {
            this.chatters.remove(pname);
        }
    }

    private void sendToChat(final String msg, final CommandSender sender) {
        String pname = sender.getName();
        if (sender instanceof Player) {
            pname = ((Player)sender).getDisplayName();
        }
        Object[] onlinePlayers = this.getServer().getOnlinePlayers().toArray();
        for (int length = onlinePlayers.length, i = 0; i < length; ++i) {
            Player p = null;
            if (onlinePlayers[i] instanceof Player)
                p = (Player)onlinePlayers[i];
            if (this.canUseAdminChat(p)) {
                p.sendMessage(String.valueOf(this.colorizeString(this.getConfig().getString("prefix"))) + " <" + ChatColor.RESET + pname + ChatColor.WHITE + "> " + msg);
            }
        }
        SimpleOpChat.logger.info("[SimpleOpChat] <" + pname + "> " + msg);
    }

    private String colorizeString(final String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
