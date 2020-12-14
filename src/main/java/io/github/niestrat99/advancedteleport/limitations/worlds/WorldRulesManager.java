package io.github.niestrat99.advancedteleport.limitations.worlds;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.limitations.worlds.list.StopIntoRule;
import io.github.niestrat99.advancedteleport.limitations.worlds.list.StopOutOfRule;
import io.github.niestrat99.advancedteleport.limitations.worlds.list.StopWithinRule;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldRulesManager {

    private HashMap<String, List<WorldRule>> rules;

    public WorldRulesManager() {
        rules = new HashMap<>();
        ConfigurationSection worlds = NewConfig.getInstance().WORLD_RULES.get();
        for (String world : worlds.getKeys(false)) {
            addWorld(world, worlds.getString(world));
        }
    }

    private void addWorld(String world, String rulesRaw) {
        String[] rules = rulesRaw.split(";");
        List<WorldRule> ruleList = new ArrayList<>();
        for (String rule : rules) {
            if (rule.startsWith("stop-teleportation-out")) {
                ruleList.add(new StopOutOfRule(rule.replaceFirst("stop-teleportation-out", "")));
            } else if (rule.startsWith("stop-teleportation-within")) {
                ruleList.add(new StopWithinRule(rule.replaceFirst("stop-teleportation-within", "")));
            } else if (rule.startsWith("stop-teleportation-into")) {
                ruleList.add(new StopIntoRule(rule.replaceFirst("stop-teleportation-into", "")));
            }
        }
        this.rules.put(world, ruleList);
    }

    public boolean canTeleport(Player player, Location toLoc) {
        String world = player.getLocation().getWorld().getName();
        List<WorldRule> rules = this.rules.get(world);
        if (rules == null || rules.isEmpty()) return true;
        for (WorldRule rule : rules) {
            if (!rule.canTeleport(player, toLoc)) return false;
        }
        return true;
    }
}
