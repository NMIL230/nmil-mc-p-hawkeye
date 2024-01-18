package nmil.mceeg.plugin.buildmaster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BuildMaster extends JavaPlugin implements CommandExecutor {
    private Location lobbyLocation;
    private Location platformSpawnLocation;
    private Location platformCenterLocation;
    private Location platformViewLocation;
    private Location[] difficultyLeverLocations;
    private Location tpLobbyButtonLocation;
    private Location platformRightButtonLocation;
    private Location goldenPlateLocation;
    private Location outsideLobbyLocation;
    private Location outsidePlateLocation;


    private BlockIO blockIO;
    private TextDisplay display;
    private GameStateMachine gameStateMachine;

    Map<Player, String> playerTag;

    private void getLocations() {
        World world = Bukkit.getWorld("Gelazkor");
        if (world != null) {
            setLobbyLocation(new Location(world, 3772, 123, 4166, 90, 0));
            setPlatformSpawnLocation(new Location(world, 2605, 143, 2462, -90, 0));
            setPlatformCenterLocation(new Location(world, 2614, 141, 2462));
            setPlatformViewLocation(new Location(world, 2608, 142, 2457,-90, 0));
            setTpLobbyButtonLocation(new Location(world, 2604, 142, 2457));
            setPlatformRightButtonLocation(new Location(world, 2604, 142, 2456));
            setGoldenPlateLocation(new Location(world, 3765, 123, 4155));
            setOutsideLobbyLocation(new Location(world, 3765, 121, 4133,180,0));
            setOutsidePlateLocation(new Location(world, 3765, 121, 4150));
            int x = 3763; int y = 125; int z = 4171;
            Location[] locations = new Location[10];
            for (int i = 0; i < locations.length; i++) {
                locations[i] = new Location(world, x, y, z - i, 0, 0);
            }
            setDifficultyLeverLocations(locations);
            getLogger().info("getLocations loaded: " + lobbyLocation + platformSpawnLocation);
        }
    }


    @Override
    public void onEnable() {
        getLocations();

        blockIO = new BlockIO();
        display = new TextDisplay();
        gameStateMachine = new GameStateMachine(this);
        playerTag = new HashMap<>();

        //getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerIOListener(this, gameStateMachine), this);
        getServer().getPluginManager().registerEvents(new InGameListener(this, gameStateMachine), this);
        getServer().getPluginManager().registerEvents(new OutGameListener(this, gameStateMachine), this);

        Objects.requireNonNull(this.getCommand("bm")).setExecutor(this);

        getLogger().info("BuildMaster loaded!");

    }
    @Override
    public void onDisable() {

     }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bm")) {
            if (args.length < 1) {
                sender.sendMessage("Usage: /bm <option>");
                return true;
            }

            String option = args[0];

            switch (option.toLowerCase()) {
                case "-op":
                    if (!(sender instanceof Player) || !sender.isOp()) {
                        sender.sendMessage("This command can only be used by an OP.");
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("clear")) {
                            blockIO.clearArea(this.getPlatformCenterLocation());
                        }
                        else{
                            sender.sendMessage("Invalid action. Use 'save' or 'load'.");
                        }
                        break;
                    }
                    if (args.length < 3) {
                        sender.sendMessage("Usage: /bm -op <save|load> <filename>");
                        return true;
                    }
                    String action = args[1];
                    String fileName = args[2];
                    if (action.equalsIgnoreCase("save")) {
                        if(blockIO.saveStructureToFile(this.getPlatformCenterLocation(), fileName)){
                            display.sendChatToAllPlayers("Structure saved to file: " + fileName + ".txt","green");
                        }
                    } else if (action.equalsIgnoreCase("load")) {
                        if (blockIO.loadStructureFromFile(this.getPlatformCenterLocation(), fileName)) {
                            display.sendChatToAllPlayers("Structure loaded from file: " + fileName + ".txt","green");
                        }
                    } else if (action.equalsIgnoreCase("clear")) {
                        blockIO.clearArea(this.getPlatformCenterLocation());
                    }
                    else{
                        sender.sendMessage("Invalid action. Use 'save' or 'load'.");
                    }
                    break;

                case "lobby":
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        player.teleport(this.getLobbyLocation());
                    } else {
                        sender.sendMessage("This command can only be used by a player.");
                    }
                    break;

                case "help":
                    sender.sendMessage("Available commands:");
                    sender.sendMessage("/bm lobby - Teleport to the lobby");
                    sender.sendMessage("/bm help - Display this help message");
                    sender.sendMessage("/bm -op clear - Clear structure (OP only)");
                    sender.sendMessage("/bm -op save <filename> - Save structure (OP only)");
                    sender.sendMessage("/bm -op load <filename> - Load structure (OP only)");
                    break;

                default:
                    sender.sendMessage("Unknown command. Use '/bm help' for a list of commands.");
                    break;
            }

            return true;
        }

        return false;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public Location getPlatformSpawnLocation() {
        return platformSpawnLocation;
    }

    public void setPlatformSpawnLocation(Location platformSpawnLocation) {
        this.platformSpawnLocation = platformSpawnLocation;
    }

    public Location[] getDifficultyLeverLocations() {
        return difficultyLeverLocations;
    }

    public void setDifficultyLeverLocations(Location[] difficultyLeverLocations) {
        this.difficultyLeverLocations = difficultyLeverLocations;
    }




    public Location getTpLobbyButtonLocation() {
        return tpLobbyButtonLocation;
    }

    public void setTpLobbyButtonLocation(Location tpLobbyButtonLocation) {
        this.tpLobbyButtonLocation = tpLobbyButtonLocation;
    }

    public Location getPlatformCenterLocation() {
        return platformCenterLocation;
    }

    public void setPlatformCenterLocation(Location platformCenterLocation) {
        this.platformCenterLocation = platformCenterLocation;
    }
    public Location getPlatformViewLocation() {
        return platformViewLocation;
    }

    public void setPlatformViewLocation(Location platformViewLocation) {
        this.platformViewLocation = platformViewLocation;
    }

    public Location getPlatformRightButtonLocation() {
        return platformRightButtonLocation;
    }

    public void setPlatformRightButtonLocation(Location platformRightButtonLocation) {
        this.platformRightButtonLocation = platformRightButtonLocation;
    }
    public Location getGoldenPlateLocation() {
        return goldenPlateLocation;
    }

    public void setGoldenPlateLocation(Location goldenPlateLocation) {
        this.goldenPlateLocation = goldenPlateLocation;
    }

    public Location getOutsideLobbyLocation() {
        return outsideLobbyLocation;
    }

    public void setOutsideLobbyLocation(Location outsideLobbyLocation) {
        this.outsideLobbyLocation = outsideLobbyLocation;
    }
    public Location getOutsidePlateLocation() {
        return outsidePlateLocation;
    }

    public void setOutsidePlateLocation(Location outsidePlateLocation) {
        this.outsidePlateLocation = outsidePlateLocation;
    }

}
