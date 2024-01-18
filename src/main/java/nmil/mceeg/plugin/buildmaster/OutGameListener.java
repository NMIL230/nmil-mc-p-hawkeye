package nmil.mceeg.plugin.buildmaster;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Material;

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
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
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
        Action action = event.getAction();

        if (block != null) {
            BlockData blockData = block.getBlockData();

            // golden plate
            if (action == Action.PHYSICAL) {
                if (event.getClickedBlock().getLocation().equals(plugin.getGoldenPlateLocation())) {
                    plugin.playerTag.computeIfPresent(player, (k, v)->{
                        if (v.equals("victor")) {
                            player.teleport(plugin.getOutsideLobbyLocation());
                            player.sendTitle("Victor","§6" + "Peak to world, who's might?", 10, 70, 20);
                        }
                        return v;
                    });
                } else if (event.getClickedBlock().getLocation().equals(plugin.getOutsidePlateLocation())){
                    plugin.playerTag.computeIfPresent(player, (k, v)->{
                        if (v.equals("victor")) {
                            player.teleport(plugin.getLobbyLocation());
                            player.sendTitle("Welcome Home","§6" + "Home at last, who's delight?", 10, 70, 20);
                        }
                        return v;
                    });
                }

            }

            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null) {
                    Material clickedMaterial = clickedBlock.getType();
                    if (isSign(clickedMaterial) && !player.isOp()) {
                        event.setCancelled(true);
                    }
                }
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Material itemInHand = event.getMaterial();
                    if (itemInHand == Material.WATER_BUCKET || itemInHand == Material.LAVA_BUCKET) {
                        event.setCancelled(true);
                    }
                }
            }


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

    private boolean isSign(Material material) {
        return material == Material.OAK_SIGN || material == Material.SPRUCE_SIGN ||
                material == Material.BIRCH_SIGN || material == Material.JUNGLE_SIGN ||
                material == Material.ACACIA_SIGN || material == Material.DARK_OAK_SIGN ||
                material == Material.CRIMSON_SIGN || material == Material.WARPED_SIGN ||
                material == Material.OAK_WALL_SIGN || material == Material.SPRUCE_WALL_SIGN ||
                material == Material.BIRCH_WALL_SIGN || material == Material.JUNGLE_WALL_SIGN ||
                material == Material.ACACIA_WALL_SIGN || material == Material.DARK_OAK_WALL_SIGN ||
                material == Material.MANGROVE_WALL_SIGN || material == Material.WARPED_WALL_SIGN ||
                material == Material.CHERRY_WALL_SIGN || material == Material.CRIMSON_WALL_SIGN ||
                material == Material.BAMBOO_WALL_HANGING_SIGN;

    }

}
