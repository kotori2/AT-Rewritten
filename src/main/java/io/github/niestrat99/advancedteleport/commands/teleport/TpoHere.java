package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TpoHere implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.admin.tpohere")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length>0) {
                        if (args[0].equalsIgnoreCase(player.getName())) {
                            CustomMessages.sendMessage(sender, "Error.requestSentToSelf");
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            if (sender.hasPermission("at.admin.tpohere.offline")) {
                                NBTReader.setLocation(args[0], player.getLocation(), new NBTReader.NBTCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean data) {
                                        sender.sendMessage("Teleported offline player " + args[0]);
                                    }

                                    @Override
                                    public void onFail(String message) {
                                        sender.sendMessage(message);
                                    }
                                });
                                return true;
                            }
                            CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                        } else {
                            CustomMessages.sendMessage(sender, "Teleport.teleportingPlayerToSelf", "{player}", target.getName());
                            CustomMessages.sendMessage(target, "Teleport.teleportingSelfToPlayer", "{player}", sender.getName());
                            PaperLib.teleportAsync(target, player.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noPermission");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }
}
