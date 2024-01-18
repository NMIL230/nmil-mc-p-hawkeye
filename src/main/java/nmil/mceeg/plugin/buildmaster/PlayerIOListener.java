package nmil.mceeg.plugin.buildmaster;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerIOListener implements Listener {
    private Location spawnLocation;
    private TextDisplay display;
    private BuildMaster plugin;
    private GameStateMachine gameStateMachine;


    public PlayerIOListener(BuildMaster plugin, GameStateMachine gameStateMachine) {
        this.spawnLocation = plugin.getLobbyLocation();
        this.plugin = plugin;
        this.display = new TextDisplay();
        this.gameStateMachine = gameStateMachine;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            player.teleport(spawnLocation);
            plugin.playerTag.computeIfAbsent(player, k -> "regular");
        } else {
            plugin.playerTag.put(player, "victor");
        }

        display.sendChatToPlayer(player,"Build Master: Welcome! Use '/bm help' for a list of commands.","aqua");

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == gameStateMachine.getPlayer()) {
            gameStateMachine.endGameOP();
        }

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(spawnLocation);
    }
}
