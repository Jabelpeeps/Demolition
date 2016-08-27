package mc.euro.demolition.tracker;

import java.util.List;

import org.bukkit.Bukkit;

import mc.alk.arena.controllers.tracker.TrackerInterface;
import mc.alk.arena.objects.tracker.Stat;
import mc.alk.arena.objects.tracker.StatType;
import mc.alk.arena.objects.tracker.WLTRecord.WLT;
import mc.alk.arena.tracker.Tracker;
import mc.euro.demolition.BombPlugin;

/**
 * Wrapper to handle any calls to BattleTracker::TrackerInterface.
 * 
 * @author Nikolai
 */
public class PlayerStats {
    BombPlugin plugin;
    public TrackerInterface tracker;
    boolean bt_enabled;
//    Version battletracker; // BattleTracker
//    Version enjin;
    
    public PlayerStats(String x) {
        plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
//        loadTracker(x);
//        loadEnjin();
    }
    
//    public boolean isEnabled() {
//        return bt_enabled;
//    }
//    
//    private void loadEnjin() {
//        this.enjin = VersionFactory.getPluginVersion("EnjinMinecraftPlugin");
//        if (enjin.isCompatible("2.6")) {
//            plugin.getLogger().info("EnjinMinecraftPlugin found & enabled.");
//        } else {
//            plugin.getLogger().info("EnjinMinecraftPlugin was not found or not compatible.");
//        }
//    }
//
//    private void loadTracker(String i) {
//        Tracker t =  Bukkit.getPluginManager().getPlugin("BattleTracker");
//        this.battletracker = VersionFactory.getPluginVersion("BattleTracker");
//        if (t != null){
//            bt_enabled = true;
//            tracker = Tracker.getInterface(i);
//            tracker.stopTracking(Bukkit.getServer().getOfflinePlayer(plugin.getFakeName()));
//        } else {
//            bt_enabled = false;
//            plugin.getLogger().warning("BattleTracker turned off or not found.");
//        }
//    }

    public void addPlayerRecord(String name, String bombs, String wlt) {
        if (Tracker.isEnabled()) {
            tracker.addPlayerRecord(name, bombs, WLT.valueOf(wlt));
        }
        /*
        if (enjin.isCompatible("2.6.0")) {
            StatsPlayer enjinStats = new StatsPlayer(Bukkit.getOfflinePlayer(name));
            String statName = null;
            if (wlt.equalsIgnoreCase("WIN")) statName = "Bases Destroyed Successfully";
            if (wlt.equalsIgnoreCase("LOSS")) statName = "Bomb Detonation Failures";
            if (wlt.equalsIgnoreCase("TIE")) statName = "Bombs Defused";
            if (statName != null) enjinStats.addCustomStat("BombArena", statName, 1, true);
        }*/
    }

    public List<Stat> getTopXWins(int n) {
        return tracker.getTopXWins(n);
    }

    public List<Stat> getTopX(StatType statType, int n) {
        return tracker.getTopX(statType, n);
    }
    
}
