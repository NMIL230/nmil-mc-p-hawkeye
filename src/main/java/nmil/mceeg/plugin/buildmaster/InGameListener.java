package nmil.mceeg.plugin.buildmaster;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InGameListener implements Listener {
    private BuildMaster plugin;
    private TextDisplay display;
    private GameStateMachine gameStateMachine;

    private Location center;

    public InGameListener(BuildMaster plugin, GameStateMachine gameStateMachine) {
        this.plugin = plugin;
        this.display = new TextDisplay();
        this.gameStateMachine = gameStateMachine;
        this.center = plugin.getPlatformCenterLocation();
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) {
            if (gameStateMachine.getCurrentGameState() != GameStateMachine.GameState.GS3_Build) {
                event.setCancelled(true);
                player.sendMessage("You are not allowed to break blocks!");
                return;
            }
            Material breakedMaterial = event.getBlock().getType();

            if (notWoolOrCarpet(breakedMaterial)) {
                event.setCancelled(true);
                player.sendMessage("You can only break building blocks!");
                return;
            }

            Map<Material, GameStateMachine.ItemCountsPair> playerItemCounts = gameStateMachine.getPlayerItemCounts();

            playerItemCounts.computeIfPresent(breakedMaterial, (key, pair) -> {
                if (pair.current > 0 && pair.current < pair.original) {
                    pair.current++;
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null && itemStack.getType() == breakedMaterial) {
                            itemStack.setAmount(itemStack.getAmount() + 1);
                            break;
                        }
                    }
                } else if (pair.current <= 0){
                    pair.current++;
                    player.getInventory().setItem(pair.slot, new ItemStack(breakedMaterial, 1));
                } else {
                    pair.current = pair.original;
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null && itemStack.getType() == breakedMaterial) {
                            itemStack.setAmount(pair.original);
                            break;
                        }
                    }
                }
                return pair;
            });

        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            if (gameStateMachine.getCurrentGameState() != GameStateMachine.GameState.GS3_Build) {
                event.setCancelled(true);
                player.sendMessage("You are not allowed to place blocks!");
                return;
            }
            if (!isInBuildArea(event.getBlock().getLocation(), plugin.getPlatformCenterLocation())) {
                event.setCancelled(true);
                player.sendMessage("You can only place blocks within the building area!");
                return;
            }
            Material placedMaterial = event.getBlock().getType();
            Map<Material, GameStateMachine.ItemCountsPair> playerItemCounts = gameStateMachine.getPlayerItemCounts();
            if (notWoolOrCarpet(placedMaterial) || !playerItemCounts.containsKey(placedMaterial)) {
                event.setCancelled(true);
                player.sendMessage("You can only place building blocks!");
                return;
            }
            playerItemCounts.computeIfPresent(placedMaterial, (key, pair) -> {
                if (pair.current > 0) {
                    pair.current--; //real time update!!
                    player.getInventory().clear();
                    //give all blocks again
                    for (Map.Entry<Material, GameStateMachine.ItemCountsPair> entryPair : playerItemCounts.entrySet()) {
                        if ( placedMaterial.equals(entryPair.getKey()) && entryPair.getValue().current ==  0) {
                            continue;
                        }
                        player.getInventory().setItem(entryPair.getValue().slot,
                                new ItemStack(entryPair.getKey(), entryPair.getValue().current));
                    }
                } else {
                    event.setCancelled(true);
                    player.sendMessage("You've run out of " + placedMaterial.name() + "!");
                }
                return pair;
            });
        }


    }
    private boolean notWoolOrCarpet(Material material) {
        return !(material == Material.WHITE_WOOL || material == Material.ORANGE_WOOL ||
                material == Material.MAGENTA_WOOL || material == Material.LIGHT_BLUE_WOOL ||
                material == Material.YELLOW_WOOL || material == Material.LIME_WOOL ||
                material == Material.PINK_WOOL || material == Material.GRAY_WOOL ||
                material == Material.LIGHT_GRAY_WOOL || material == Material.CYAN_WOOL ||
                material == Material.PURPLE_WOOL || material == Material.BLUE_WOOL ||
                material == Material.BROWN_WOOL || material == Material.GREEN_WOOL ||
                material == Material.RED_WOOL || material == Material.BLACK_WOOL ||
                material == Material.WHITE_CARPET || material == Material.ORANGE_CARPET ||
                material == Material.MAGENTA_CARPET || material == Material.LIGHT_BLUE_CARPET ||
                material == Material.YELLOW_CARPET || material == Material.LIME_CARPET ||
                material == Material.PINK_CARPET || material == Material.GRAY_CARPET ||
                material == Material.LIGHT_GRAY_CARPET || material == Material.CYAN_CARPET ||
                material == Material.PURPLE_CARPET || material == Material.BLUE_CARPET ||
                material == Material.BROWN_CARPET || material == Material.GREEN_CARPET ||
                material == Material.RED_CARPET || material == Material.BLACK_CARPET);
    }


    private boolean isInBuildArea(Location blockLocation, Location centerLocation) {
        int radius = 6; // Half of 13 (13x13x13 area)
        int radiusY = 12;
        return Math.abs(blockLocation.getX() - centerLocation.getX()) <= radius &&
                Math.abs(blockLocation.getY() - centerLocation.getY()) <= radiusY &&
                Math.abs(blockLocation.getZ() - centerLocation.getZ()) <= radius;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameStateMachine.getCurrentGameState() != GameStateMachine.GameState.GAME_OVER) {
            Player player = event.getPlayer();
            Location playerLocation = player.getLocation();

            double distanceX = Math.abs(playerLocation.getX() - center.getX());
            double distanceY = Math.abs(playerLocation.getY() - center.getY());
            double distanceZ = Math.abs(playerLocation.getZ() - center.getZ());

            if (!gameStateMachine.playerLeft && ( distanceX > 12 || distanceZ > 16 || distanceY > 13)) {
                gameStateMachine.onPlayerLeave(player,center);
            }
        }
    }
    @EventHandler
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
            if (gameStateMachine.getCurrentGameState() == GameStateMachine.GameState.GS3_Build && player == gameStateMachine.getPlayer()) {
                gameStateMachine.submitBuild();
            }
        }
    }
}
