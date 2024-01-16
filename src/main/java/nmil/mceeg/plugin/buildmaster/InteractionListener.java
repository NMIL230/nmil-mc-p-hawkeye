package nmil.mceeg.plugin.buildmaster;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.block.Block;
import org.bukkit.material.Lever;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InteractionListener implements Listener {
    private BuildMaster plugin;

    public InteractionListener(BuildMaster plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        BlockData blockData = block.getBlockData();


        if (blockData instanceof Switch) {
            if (true) {
                //save
            } else if (true) {
                //
            }
            int difficulty = getDifficultyFromLeverLocation(block.getLocation());
            if (difficulty != 0) {
                plugin.setDifficulty(difficulty);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.teleport(plugin.getPlatform_location());
                }
            }
        }
    }

    private int getDifficultyFromLeverLocation(Location location) {
        Location[] difficultyLever_locations = plugin.getDifficultyLever_locations();
        if (difficultyLever_locations == null) {
            return 0;
        }

        for (int i = 0; i < difficultyLever_locations.length; i++) {
            if (difficultyLever_locations[i].equals(location)) {
                return i + 1;
            }
        }

        return 0;
    }
}
