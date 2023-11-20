package nmil.mceeg.plugin;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
public class PlayerEventListener implements Listener {
    private final Hawkeye plugin;
    private final EventListenerCallback callback;
    public PlayerEventListener(Hawkeye plugin, EventListenerCallback callback) {
        this.plugin = plugin;
        this.callback = callback;
    }

    // Player interaction event
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        String interactedObject = "something";
        if (clickedBlock != null) {
            interactedObject = "block " + clickedBlock.getType();
        }

        String itemInfo = (itemInHand != null) ? " using " + itemInHand.getType() : "";
        String eventInfo = "player" + " " + action + " with " + interactedObject + itemInfo;
        callback.handleEventInfo(player, eventInfo);

    }
    // Block break event
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " broke " + event.getBlock().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Block place event
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " placed " + event.getBlock().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Block damage event
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " damaged " + event.getBlock().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Player interacts with entity event
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " interacted with " + event.getRightClicked().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Player attacks entity event
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            String eventInfo = "player" + " attacked " + event.getEntity().getType();
            callback.handleEventInfo(player, eventInfo);
        }
    }

    // Player gets hurt event
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            String eventInfo = "player" + " got hurt by " + event.getCause();
            callback.handleEventInfo(player, eventInfo);
        }
    }

    // Player shears entity event
    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " sheared " + event.getEntity().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Player consumes item event
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " consumed " + event.getItem().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Player changes held item event
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " changed held item";
        callback.handleEventInfo(player, eventInfo);
    }

    // Player drops item event
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " dropped " + event.getItemDrop().getItemStack().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Player picks up item event
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " picked up " + event.getItem().getItemStack().getType();
        callback.handleEventInfo(player, eventInfo);
    }

    // Player interacts with inventory event
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            String eventInfo = "player" + " clicked in inventory at slot " + event.getSlot();
            callback.handleEventInfo(player, eventInfo);
        }
    }


}
