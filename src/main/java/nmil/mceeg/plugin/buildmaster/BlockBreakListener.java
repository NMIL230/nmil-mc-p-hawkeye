package nmil.mceeg.plugin.buildmaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!player.isOp()) {
            if (block.getType() == Material.DIAMOND_BLOCK) {
                event.setCancelled(true);
                player.sendMessage("You are not allowed to break this block!");
            }
        }
    }
}
