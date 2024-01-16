package nmil.mceeg.plugin.buildmaster;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerSpawnListener implements Listener {
    private Location spawnLocation;

    public PlayerSpawnListener(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.teleport(spawnLocation);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(spawnLocation);
    }
}
