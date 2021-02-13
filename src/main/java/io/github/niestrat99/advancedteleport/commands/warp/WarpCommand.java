package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class WarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.get().USE_WARPS.get()) {
            if (args.length > 0) {
                if (sender.hasPermission("at.member.warp")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        int cooldown = CooldownManager.secondsLeftOnCooldown("warp", player);
                        if (cooldown > 0) {
                            CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
                            return true;
                        }
                        if (Warp.getWarps().containsKey(args[0])) {
                            if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                                CustomMessages.sendMessage(player, "Error.onCountdown");
                                return true;
                            }
                            Warp warp = Warp.getWarps().get(args[0]);
                            warp(warp, player);
                        } else {
                            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
                        }
                    } else {
                        CustomMessages.sendMessage(sender, "Error.notAPlayer");
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.noPermission");
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }

    public static void warp(Warp warp, Player player) {
        boolean found = false;
        if (player.hasPermission("at.member.warp.*")) found = true;
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().equalsIgnoreCase("at.member.warp." + warp.getName().toLowerCase())) {
                found = permission.getValue();
                break;
            }
        }
        if (!found) {
            CustomMessages.sendMessage(player, "Error.noPermissionWarp", "{warp}", warp.getName());
            return;
        }
        ATTeleportEvent event = new ATTeleportEvent(player, warp.getLocation(), player.getLocation(), warp.getName(), ATTeleportEvent.TeleportType.WARP);
        if (!event.isCancelled()) {
            if (PaymentManager.getInstance().canPay("warp", player)) {
                int warmUp = NewConfig.get().WARM_UPS.WARP.get();
                if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    MovementManager.createMovementTimer(player, warp.getLocation(), "warp", "Teleport.teleportingToWarp", warmUp, "\\{warp}", warp.getName());
                    // If the cooldown is to be applied after request or accept (they are the same in the case of /warp), apply it now
                    String cooldownConfig = NewConfig.get().APPLY_COOLDOWN_AFTER.get();
                    if(cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
                        CooldownManager.addToCooldown("warp", player);
                    }
                } else {
                    PaymentManager.getInstance().withdraw("warp", player);
                    PaperLib.teleportAsync(player, warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToWarp").replaceAll("\\{warp}", warp.getName()));
                }
            }
        }
    }
}
