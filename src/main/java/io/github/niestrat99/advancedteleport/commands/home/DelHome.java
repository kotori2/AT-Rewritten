package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHome extends AbstractHomeCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NewConfig.get().USE_HOMES.get()) {
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (sender.hasPermission("at.member.delhome")) {
                    if (args.length>0) {
                        if (sender.hasPermission("at.admin.delhome")) {
                            if (args.length>1) {
                                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                                delHome(target, player, args[1]);
                                return true;
                            }
                        }
                        delHome(player, args[0]);
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noHomeInput");
                    }
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.notAPlayer");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }

    private void delHome(OfflinePlayer player, Player sender, String name) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (!atPlayer.hasHome(name)) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return;
        }
        atPlayer.removeHome(name, data -> {
            if (sender.getUniqueId() == player.getUniqueId()) {
                CustomMessages.sendMessage(sender, "Info.deletedHome", "{home}", name);
            } else {
                CustomMessages.sendMessage(sender, "Info.deletedHomeOther", "{home}", name, "{player}", player.getName());
            }
        });
    }

    private void delHome(Player player, String name) {
        delHome(player, player, name);
    }
}
