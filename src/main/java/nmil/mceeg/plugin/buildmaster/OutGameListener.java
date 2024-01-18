package nmil.mceeg.plugin.buildmaster;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class OutGameListener implements Listener {
    private BuildMaster plugin;
    private TextDisplay display;
    private GameStateMachine gameStateMachine;

    public OutGameListener(BuildMaster plugin, GameStateMachine gameStateMachine) {
        this.plugin = plugin;
        this.display = new TextDisplay();
        this.gameStateMachine = gameStateMachine;
    }




    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!player.isOp()) {
                event.setCancelled(true);
                player.sendMessage("You are not allowed to attack!");
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof Switch) {
                Location blockLocation = block.getLocation();
                // TP to LOBBY
                if (blockLocation.equals(plugin.getTpLobbyButtonLocation())) {
                    gameStateMachine.endGamePlayer();
                }
                // End the game
                if (blockLocation.equals(plugin.getPlatformRightButtonLocation())) {
                    if (player.isOp()) {
                        display.sendChatToPlayer(player,"Game Over","gold");
                        gameStateMachine.endGameOP();
                    } else {
                        display.sendChatToPlayer(player,"Server Operator ONLY","gold");
                    }

                }
                // Difficulty Selection
                else {
                    int difficulty = getDifficultyFromLeverLocation(blockLocation);
                    if (difficulty!= 0) {
                        gameStateMachine.setDifficulty(difficulty);
//                        ((Powerable) blockData).setPowered(ture);
//                        block.setBlockData(blockData);
////                        state.update(true, true);
//                        display.sendChatToPlayer(player,"set false","red");
                        player.teleport(plugin.getPlatformSpawnLocation());

                        gameStateMachine.onGame(player, difficulty);
                    }
                }
            }
        }
    }

    private int getDifficultyFromLeverLocation(Location location) {
        Location[] difficultyLeverLocations = plugin.getDifficultyLeverLocations();
        if (difficultyLeverLocations == null) {
            return 0;
        }

        for (int i = 0; i < difficultyLeverLocations.length; i++) {
            if (difficultyLeverLocations[i].equals(location)) {
                return i + 1;
            }
        }

        return 0;
    }
}
