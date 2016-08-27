package mc.euro.demolition.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.tracker.PlayerStat;
import mc.alk.arena.objects.tracker.Stat;
import mc.alk.arena.objects.tracker.StatType;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.tracker.Tracker;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.arenas.EodArena;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.debug.DebugOn;
import mc.euro.demolition.sound.SoundAdapter;
import mc.euro.demolition.sound.SoundPlayer;
import mc.euro.demolition.util.BaseType;

/**
 * Commands shared by BombArena & SndArena. <br/><br/>
 * 
 * @author Nikolai
 */
public class EodExecutor extends CustomCommandExecutor {
    
    BombPlugin plugin;
    
    public EodExecutor(BombPlugin reference) {
        plugin = reference;
    }
    
    @MCCommand( cmds = {"addbase"}, perm = "bombarena.addbase", usage = "addbase <arena>" )
    public boolean addbase(Player sender, Arena a) {
        if (!(a instanceof EodArena)) {
            sender.sendMessage("Arena must be a valid BombArena or SndArena.");
            return false;
        }
        EodArena arena = (EodArena) a;
        Location loc = sender.getLocation();
        Location base_loc = plugin.getExactLocation(loc);
        
        if (base_loc == null) {
            sender.sendMessage("addbase command failed to find a BaseBlock near your location.");
            sender.sendMessage("Please set BaseBlocks in the arena (1 or more).");
            sender.sendMessage("If you have already set BaseBlocks, then stand closer and re-run the command.");
            return true;
        }
        
        arena.addSavedBase(base_loc);
        ArenaSerializer.saveArenas(plugin); // silently save
        sender.sendMessage("Base added to arena: " + a.getName());
        return true;
    }
    
    @MCCommand( cmds = {"removebase", "deletebase"}, perm = "bombarena.addbase", usage = "removebase <arena>")
    public boolean removeBase(Player sender, Arena a) {
        if (!(a instanceof EodArena)) {
            sender.sendMessage("Arena must be a valid BombArena or SndArena.");
            return false;
        }
        EodArena arena = (EodArena) a;
        Location loc = sender.getLocation();
        List<Location> allbases = arena.getCopyOfSavedBases();
        
        for (Location base : allbases) {
            if (base.distance(sender.getLocation()) <= 6) {
                arena.removeSavedBase(base);
                sender.sendMessage("Base (" + base.toVector().toBlockVector().toString() + ") has been removed.");
                if (base.getBlock().getType() == plugin.getBaseBlock()) {
                    base.getBlock().setType(Material.AIR);
                }
            }
        }      
        return true;
    }
    
    @MCCommand( cmds = {"removeallbases", "deleteallbases", "clearallbases"}, 
                perm = "bombarena.addbase", 
                usage = "clearallbases <arena>" )
    public boolean clearAllBases(CommandSender sender, Arena a) {
        if (!(a instanceof EodArena)) {
            sender.sendMessage("Arena must be a valid BombArena or SndArena.");
            return false;
        }
        EodArena arena = (EodArena) a;
        for (Location loc : arena.getCopyOfSavedBases()) {
            if (loc.getBlock().getType() == plugin.getBaseBlock()) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        arena.clearSavedBases();
        sender.sendMessage("All bases for arena (" + arena.getName() + ") have been cleared.");
        return false;
    }
    
    @MCCommand( cmds = {"spawnbomb"}, perm = "bombarena.spawnbomb", usage = "spawnbomb <arena>" )
    public boolean spawnbomb( Player sender, String a ) {
        plugin.debug.log("arena = " + a);
        Arena arena = BattleArenaController.getArena( a );
        if ( arena == null ) return false;
        
        Material bomb = plugin.getBombBlock();
        int matchTime = arena.getParams().getMatchTime();
        
        plugin.debug.log("spawnbomb() MatchTime = " + matchTime);
        
        String selectArena = "aa select " + arena.getName();
        String addSpawn = "aa addspawn " + bomb.name() + " fs=1 rs=300 ds=" + matchTime + " index=1";
        
        if ( plugin.getServer().dispatchCommand( sender, selectArena ) 
                && plugin.getServer().dispatchCommand( sender, addSpawn ) ) {
            sender.sendMessage( "The bomb spawn for " + arena.getName() + " has been set!" );
            return true;
        }
        sender.sendMessage("The spawnbomb command has failed.");
        return false;
    }
    
    @MCCommand( cmds = {"stats"}, perm = "bomb.stats", usage = "stats" )
    public boolean stats(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Invalid command syntax: Please specify a player name");
            sender.sendMessage("./bomb stats <player>");
            sender.sendMessage("or /bomb stats top X");
            return true;
        }
        stats(sender, plugin.getServer().getOfflinePlayer(sender.getName()));
        return true;
    }
    
    @MCCommand( cmds = {"stats"}, perm = "bomb.stats.other", usage = "stats <player>" )
    public boolean stats(CommandSender sender, OfflinePlayer p) {
        if (!Tracker.isEnabled()) {
            plugin.getLogger().warning("BattleTracker not found or turned off.");
            sender.sendMessage("BombArena statistics are not being tracked.");
            return true;
        }
        if (p == null) {
            sender.sendMessage("No such player. Did you misspell their name ?");
            return true;
        }
        PlayerStat ps = Tracker.getInterface( "BombArena" ).getPlayerRecord(p);
        int wins = ps.getWins();
        int total = wins + ps.getLosses();
        int percentage = (total == 0) ? 0 : (int)  (wins * 100.00) / total;
        String intro = ( sender.getName().equalsIgnoreCase(p.getName())) ? "You have" 
                                                                         : p.getName() + " has";
        
        sender.sendMessage( intro + " successfully destroyed the other teams base " + wins + " times out of " + 
                                 total + " attempts. (" + percentage + "%)" );
        sender.sendMessage( intro + " defused the bomb " + ps.getTies() + " times." );
        return true;
    }
    
    /**
     * Shows bomb arena stats for the command sender.
     * Example Usage: /bomb stats top 5
     */
    @MCCommand( cmds = {"stats"}, subCmds = {"top"}, perm = "bomb.stats.top", usage = "stats top X" )
    public boolean stats(CommandSender cs, Integer n) {
            if (!Tracker.isEnabled()) {
                plugin.getLogger().warning(ChatColor.AQUA + "BattleTracker not found or turned off.");
                cs.sendMessage(ChatColor.YELLOW + "Bomb Arena statistics are not being tracked.");
                return true;
            }
            
            List<Stat> planted = Tracker.getInterface( "BombArena" ).getTopXWins(n);
            cs.sendMessage(ChatColor.AQUA  +  "Number of Bombs Planted");
            cs.sendMessage(ChatColor.YELLOW + "-----------------------");
            int i = 1;
            for (Stat w : planted) {
                if (w.getName().equalsIgnoreCase(plugin.getFakeName())) {
                    continue;
                }
                int total = w.getWins() + w.getLosses();
                int percentage = (total == 0) ? 0 : (int) (w.getWins() * 100.00) / total;
                
                cs.sendMessage("" + i + " " + w.getName() + " " + w.getWins() + " out of " + 
                                            total + " (" + percentage + "%)");
                i = i + 1;
            }
            
            List<Stat> defused = Tracker.getInterface( "BombArena" ).getTopX(StatType.TIES, n);
            cs.sendMessage(ChatColor.AQUA + "Number of Bombs Defused");
            cs.sendMessage(ChatColor.YELLOW + "-----------------------");
            i = 1;
            
            for (Stat d : defused) {
                if (d.getName().equalsIgnoreCase(plugin.getFakeName())) continue;
                cs.sendMessage("" + i + " " + d.getName() + " " + d.getTies());
                i = i + 1;
            }
            
            return true;
    }
    
    /**
     * For use after you edit config.yml
     */
    @MCCommand( cmds = {"reloadconfig"}, perm = "bombarena.setconfig" )
    public boolean reloadConfig(CommandSender sender) {
        plugin.reloadConfig();
        plugin.loadConfigYml();
        return true;
    }
    
    @MCCommand( cmds = {"setconfig"}, subCmds = {"TimerSound"},
                perm = "bombarena.setconfig", 
                usage = "setconfig TimerSound <SOUND_NAME>" )
    public boolean setTimerSound(CommandSender sender, String sound_name) {
        return verifySound(sender, "TimerSound", sound_name);
    }
    
    @MCCommand( cmds = {"setconfig"}, subCmds = {"PlantDefuseNoise", "Noise"},
                perm = "bombarena.setconfig", 
                usage = "setconfig Noise <SOUND_NAME>" )
    public boolean setPlantDefuseNoise(CommandSender sender, String sound_name) {
        return verifySound(sender, "PlantDefuseNoise", sound_name);
    }
    
    private boolean verifySound(CommandSender sender, String key, String value) {
        if (value.equalsIgnoreCase("NONE") || value.equalsIgnoreCase("OFF")
                || value.equalsIgnoreCase("SILENT") || value.equalsIgnoreCase("NULL")) {
            setSound(key, null);
            return true;
        }
        Sound sound = SoundAdapter.getSound(value);
        if (sound == null) {
            sender.sendMessage("" + value + " is not a valid sound.");
            return false;
        }
        setSound(key, sound);
        return true;
    }

    private void setSound(String key, Sound value) {
        plugin.getConfig().set(key, value);
        plugin.saveConfig();
        
        if (key.equals("TimerSound")) 
            plugin.setTimerSound(value);
        else if (key.equals("PlantDefuseNoise")) 
            plugin.setPlantDefuseNoise(value);
    }
    
    @MCCommand( cmds = {"setconfig"}, subCmds = {"bombblock"}, 
                perm = "bombarena.setconfig", 
                usage = "setconfig BombBlock <handItem>" )
    public boolean setBombBlock(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null) {
            p.sendMessage("There is nothing in your hand.");
            return false;
        }
        plugin.setBombBlock(hand.getType());
        p.sendMessage("BombBlock has been set to " + hand.getType());
        p.sendMessage("All of your arenas have been automatically "
                + "updated with the new BombBlock.");
        return true;
    }

    @MCCommand( cmds = {"setconfig"}, subCmds = {"baseblock"}, 
                perm = "bombarena.setconfig",
                usage = "setconfig BaseBlock <handItem>" )
    public boolean setBaseBlock(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null) {
            p.sendMessage("There is nothing in your hand.");
            return false;
        }
        if (!BaseType.containsKey(hand.getType().name())) {
            p.sendMessage("That is not a valid BaseBlock in your hand!");
            return true;
        }
        p.sendMessage("BaseBlock has been set to " + hand.getType().name());
        plugin.setBaseBlock(hand.getType());
        return true;
    }
    
    @MCCommand( cmds = {"setconfig"}, subCmds = {"databasetable"}, 
                perm = "bombarena.setconfig", 
                usage = "setconfig DatabaseTable <name>")
    public boolean setDatabaseTable(CommandSender sender, String table) {
        plugin.setDatabaseTable(table);
        sender.sendMessage("DatabaseTable has been set to " + table);
        return true;
    }
    
    @MCCommand( cmds = {"setconfig"}, perm = "bombarena.setconfig", usage = "setconfig <option> <integer>" )
    public boolean setConfig(CommandSender sender, String option, Integer value) {
        Set<String> keys = plugin.getConfig().getKeys(false);
        for (String key : keys) {
            if (option.equalsIgnoreCase(key)) {
                plugin.getConfig().set(key, value);
                sender.sendMessage("" + key + " has been set to " + value);
                plugin.saveConfig();
                plugin.loadConfigYml();
                return true;
            }
        }
        sender.sendMessage("Valid options: " + keys.toString());
        return false;
    }
    
    @MCCommand( cmds = {"listconfig"}, perm = "bombarena.setconfig", usage = "listconfig" )
    public boolean listconfig(CommandSender sender) {
        sender.sendMessage("Config options: " + plugin.getConfig().getKeys(false).toString());
        return true;
    }
    
    @MCCommand( cmds = {"setconfig"}, subCmds = {"debug"}, 
                perm = "bombarena.setconfig", 
                usage = "setconfig debug <true/false>" )
    public boolean setDebug(CommandSender sender, boolean b) {
        plugin.getConfig().set("Debug", b);
        plugin.saveConfig();
        sender.sendMessage("config.yml option 'Debug' has been set to " + b);
        plugin.loadConfigYml();
        return true;
    }

    /**
     * Toggles debug mode ON / OFF.
     * Usage: /bomb debug
     */
    @MCCommand( cmds = {"debug"}, perm = "bombarena.debug", usage = "debug" )
    public boolean toggleDebug(CommandSender sender) {
        if (plugin.debug instanceof DebugOn) {
            plugin.debug = new DebugOff(plugin);
            plugin.getConfig().set("Debug", false);
            plugin.saveConfig();
            sender.sendMessage("Debugging mode for the BombArena has been turned off.");
            return true;
        } 
        else if (plugin.debug instanceof DebugOff) {
            plugin.debug = new DebugOn(plugin);
            plugin.getConfig().set("Debug", true);
            plugin.saveConfig();
            sender.sendMessage("Debugging mode for the BombArena has been turned on.");
            return true;
        }
        return false;
    }
    
    @MCCommand( cmds = {"getname"}, perm = "bombarena.setconfig", usage = "getname <handItem>" )
    public boolean getBlockName(Player p) {
        String name = p.getInventory().getItemInMainHand().getType().name();
        p.sendMessage("You are holding " + name);
        return true;
    }
    
    @MCCommand( cmds = {"dropItemNaturally"}, op = true )
    public boolean dropItemNaturally(Player player) {
        Location location = player.getLocation();
        ItemStack item = new ItemStack(plugin.getBombBlock());
        player.getLocation().getWorld().dropItemNaturally(location, item);
        return true;
    }
    
    @MCCommand( cmds = {"dropItem"}, op = true )
    public boolean dropItem(Player player) {
        Location location = player.getLocation();
        ItemStack item = new ItemStack(plugin.getBombBlock());
        player.getLocation().getWorld().dropItem(location, item);
        return true;
    }
    
    @MCCommand( cmds = {"soundlist"}, op = true )
    public boolean soundList(CommandSender sender, Integer page) {
        int size = Sound.values().length;
        int pages = size / 10;
        int start = page * 10;
        int end = start + 10;
        end = (end > size) ? size : end;
        for (int index = start; index < end; index = index + 1) {
            String s = Sound.values()[index].name().toLowerCase();
            sender.sendMessage("" + index + ". " + s);
        }
        sender.sendMessage("Page " + page + " of " + pages);
        return true;
    }
    
    @MCCommand( cmds = {"sound"}, op = true )
    public boolean sound( Player player, String sound) {
        return sound(player, sound, 1.0f);
    }
    
    @MCCommand( cmds = {"sound"}, op = true )
    public boolean sound( Player player, String sound, float pitch) {
        float volume = 1.0f;
        Sound SOUND = SoundAdapter.getSound(sound); // Sound.valueOf(sound.toUpperCase());
        if (SOUND == null) {
            player.sendMessage("Sound not found");
            return false;
        }

        long delay = 0L;
        for (int n = 0; n < 5; n++) {
            new SoundPlayer(player, SOUND, volume, pitch).runTaskLater(plugin, delay);
            delay = delay + 20L;
        }
        return true;
    }
    
    @MCCommand( cmds = {"sounds"}, subCmds = {"playAll"} )
    public boolean soundPlayAll( Player player) {
        
        long delay = 0L;
        float volume = 1F;
        
        for (Sound sound : Sound.values()) {
            for (int n = 1; n <= 4; n++) {
                float pitch = n / 2;
                new SoundPlayer(player, sound, volume, pitch).runTaskLater(plugin, delay);
                delay = delay + 20L; // delay between sounds
            }
            delay = delay + 40L; // extra delay between different sounds
        }        
        return true;
    }
}
