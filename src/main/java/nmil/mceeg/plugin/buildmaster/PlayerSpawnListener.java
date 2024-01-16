package nmil.mceeg.plugin.buildmaster;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event, Location spawnLocation) {
        event.getPlayer().setBedSpawnLocation(spawnLocation, true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event, Location respawnLocation) {
        event.setRespawnLocation(respawnLocation);
    }
}
