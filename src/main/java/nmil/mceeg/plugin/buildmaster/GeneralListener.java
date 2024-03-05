package nmil.mceeg.plugin.buildmaster;

import nmil.mceeg.plugin.buildmaster.event.BuildMasterEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import static nmil.mceeg.plugin.buildmaster.GameStateMachine.GameType.Classical;

public class GeneralListener implements Listener {
    private BuildMaster plugin;
    private TextDisplay display;
    private GameStateMachine gameStateMachine;

    private int isRainbow;

    public GeneralListener(BuildMaster plugin, GameStateMachine gameStateMachine) {
        this.plugin = plugin;
        this.display = new TextDisplay();
        this.gameStateMachine = gameStateMachine;
        gameStateMachine.setCurrentGameType(GameStateMachine.GameType.Random3D);
        changeGameModeSign("Random Rainbow","3D");
        isRainbow = 0;
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
                        display.sendChatToPlayer(player,"Game Over Lever","gold");
                        gameStateMachine.endGameOP();
                    } else {
                        //display.sendChatToPlayer(player,"Server Operator ONLY","gold");
                    }
                }
                if (blockLocation.equals(plugin.getGameModeButtonLocation())) {
                    if (isRainbow == 0) {
                        gameStateMachine.setCurrentGameType(Classical);
                        changeGameModeSign("Classical","");
                        isRainbow = 1;
                    } else if (isRainbow == 1){
                        gameStateMachine.setCurrentGameType(GameStateMachine.GameType.Random2D);
                        changeGameModeSign("Random Rainbow","2D");
                        isRainbow = 2;
                    }  else {
                        gameStateMachine.setCurrentGameType(GameStateMachine.GameType.Random3D);
                        changeGameModeSign("Random Rainbow","3D");
                        isRainbow = 0;
                    }
                }
                // Difficulty Selection
                else {
                    int difficulty = getDifficultyFromLeverLocation(blockLocation);
                    if (difficulty!= 0) {
                        gameStateMachine.setDifficulty(difficulty);
                        player.teleport(plugin.getPlatformSpawnLocation());

                        gameStateMachine.onGame(player, difficulty);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            player.teleport(plugin.getLobbyLocation());
            plugin.playerTag.computeIfAbsent(player, k -> "regular");
        } else {
            plugin.playerTag.put(player, "victor");
        }

        player.sendMessage("§bBuild Master(Arch): Welcome! Use '/bm help' for a list of commands.");

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == gameStateMachine.getPlayer()) {
            if( gameStateMachine.getCurrentGameState() != GameStateMachine.GameState.GAME_OVER) {
                gameStateMachine.endGameOP();
                Bukkit.getServer().getPluginManager().callEvent(new BuildMasterEndEvent("",player));
            }


        }


    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(plugin.getLobbyLocation());
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

    public void changeGameModeSign(String s1, String s2) {
        Location location = plugin.getGameModeSignLocation();
        Block block = location.getBlock();

        block.setType(Material.CHERRY_WALL_SIGN);

        Sign sign = (Sign) location.getBlock().getState();
        sign.setLine(0, "Game Mode");

        sign.setLine(2, s1);
        sign.setLine(3, s2);


        BlockData blockData = sign.getBlockData();
        Directional directional = (Directional) blockData;
        directional.setFacing(BlockFace.EAST);
        sign.setBlockData(directional);

        sign.update();

    }


}
