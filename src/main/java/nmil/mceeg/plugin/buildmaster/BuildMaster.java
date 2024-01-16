package nmil.mceeg.plugin.buildmaster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildMaster extends JavaPlugin {
    Location lobby_location;
    Location platform_location;
    Location[] difficultyLever_locations = new Location[10];

    private void getLocations() {
        World world = Bukkit.getWorld("Gelazkor");
        if (world != null) {
            lobby_location = new Location(world, 3772, 123, 4166, 90, 0);
            platform_location = new Location(world, 2614, 141, 2462, -90, 0);

            int x = 3763;
            int y = 125;
            int z = 4171;
            for (int i = 0; i < difficultyLever_locations.length; i++) {
                difficultyLever_locations[i] = new Location(world, x, y, z - i, 0, 0);
            }
        }
    }


    @Override
    public void onEnable() {
        getLocations();

        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(), this);

        getLogger().info("BuildMaster loaded!");

    }

    // onDisable()
}
