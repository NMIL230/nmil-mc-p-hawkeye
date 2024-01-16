package nmil.mceeg.plugin.buildmaster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class BuildMaster extends JavaPlugin implements Listener {
    private Location lobby_location;
    private Location platform_location;
    private Location[] difficultyLever_locations;
    private Location saveButton_location;
    private Location tpLobbyButton_location;

    private int difficulty;

    private void getLocations() {
        World world = Bukkit.getWorld("Gelazkor");
        if (world != null) {
            setLobby_location(new Location(world, 3772, 123, 4166, 90, 0));
            setPlatform_location(new Location(world, 2614, 141, 2462, -90, 0));

            setSaveButton_location(new Location(world, 2604, 142, 2456));
            setTpLobbyButton_location(new Location(world, 2604, 142, 2457));

            int x = 3763; int y = 125; int z = 4171;
            Location[] locations = new Location[10];
            for (int i = 0; i < locations.length; i++) {
                locations[i] = new Location(world, x, y, z - i, 0, 0);
            }
            setDifficultyLever_locations(locations);
            getLogger().info("getLocations loaded: " + lobby_location + platform_location);

        }

    }


    @Override
    public void onEnable() {
        getLocations();
//        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(lobby_location), this);
        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);

        getLogger().info("BuildMaster loaded!");

    }
    @Override
    public void onDisable() {

     }






    public Location getLobby_location() {
        return lobby_location;
    }

    public void setLobby_location(Location lobby_location) {
        this.lobby_location = lobby_location;
    }

    public Location getPlatform_location() {
        return platform_location;
    }

    public void setPlatform_location(Location platform_location) {
        this.platform_location = platform_location;
    }

    public Location[] getDifficultyLever_locations() {
        return difficultyLever_locations;
    }

    public void setDifficultyLever_locations(Location[] difficultyLever_locations) {
        this.difficultyLever_locations = difficultyLever_locations;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public Location getSaveButton_location() {
        return saveButton_location;
    }

    public void setSaveButton_location(Location saveButton_location) {
        this.saveButton_location = saveButton_location;
    }

    public Location getTpLobbyButton_location() {
        return tpLobbyButton_location;
    }

    public void setTpLobbyButton_location(Location tpLobbyButton_location) {
        this.tpLobbyButton_location = tpLobbyButton_location;
    }


}
