package nmil.mceeg.plugin.hawkeye;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerEventListener implements Listener {
    private final Hawkeye plugin;
    private final EventListenerCallback callback;
    public PlayerEventListener(Hawkeye plugin, EventListenerCallback callback) {
        this.plugin = plugin;
        this.callback = callback;
    }


    // Block break event
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        String locationString = "x=" + location.getBlockX() + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ();
        String eventInfo = "player " + player.getName() + " broke " + event.getBlock().getType() + " at [" + locationString + "]";
        callback.handleEventInfo(player, eventInfo, "event-block");
    }

    // Block damage event
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand(); // 获取玩家手中的物品

        Location location = event.getBlock().getLocation();
        String locationString = "x=" + location.getBlockX() + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ();
        String itemInfo = (itemInHand != null) ? " using " + itemInHand.getType() : " with no tool";
        String eventInfo = "player " + player.getName() + " damaged " + event.getBlock().getType() + " at [" + locationString + "]" + itemInfo;
        callback.handleEventInfo(player, eventInfo, "event-block");
    }
    // Block place event
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();
        String locationString = "x=" + location.getBlockX() + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ();
        String eventInfo = "player " + player.getName() + " placed " + event.getBlock().getType() + " at [" + locationString + "]";
        callback.handleEventInfo(player, eventInfo, "event-block");
    }
    // Player interaction event
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        String interactedObject = "something";
        String locationString = "";
        if (clickedBlock != null) {
            Location location = clickedBlock.getLocation();
            locationString = " at [x=" + location.getBlockX() + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ() + "]";
            interactedObject = "block " + clickedBlock.getType();
        }

        String itemInfo = (itemInHand != null) ? " using " + itemInHand.getType() : "";
        String eventInfo = "player " + player.getName() + " " + action + " with " + interactedObject + itemInfo + locationString;
        callback.handleEventInfo(player, eventInfo, "event-interact");
    }

    // Player interacts with entity event
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " interacted with " + event.getRightClicked().getType();
        callback.handleEventInfo(player, eventInfo,"event-interact");
    }

    // Player shears entity event
    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " sheared " + event.getEntity().getType();
        callback.handleEventInfo(player, eventInfo, "event-interact");
    }

    // Player attacks entity event
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            String eventInfo = "player" + " attacked " + event.getEntity().getType();
            callback.handleEventInfo(player, eventInfo, "event-fight");
        }
    }

    // Player gets hurt event
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            String eventInfo = "player" + " got hurt by " + event.getCause();
            callback.handleEventInfo(player, eventInfo, "event-fight");
        }
    }


    // Player consumes item event
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " consumed " + event.getItem().getType();
        callback.handleEventInfo(player, eventInfo, "event-item");
    }

    // Player changes held item event
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack newItem = inventory.getItem(event.getNewSlot()); // 获取新的物品

        String newItemInfo = (newItem != null) ? newItem.getType().toString() : "nothing";
        String eventInfo = "player " + player.getName() + " changed held item to " + newItemInfo;
        callback.handleEventInfo(player, eventInfo, "event-item");
    }

    // Player drops item event
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " dropped " + event.getItemDrop().getItemStack().getType();
        callback.handleEventInfo(player, eventInfo,"event-item");
    }

    // Player picks up item event
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        String eventInfo = "player" + " picked up " + event.getItem().getItemStack().getType();
        callback.handleEventInfo(player, eventInfo, "event-item");
    }

    // Player interacts with inventory event
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            String eventInfo = "player" + " clicked in inventory at slot " + event.getSlot();
            callback.handleEventInfo(player, eventInfo, "event-item");
        }
    }

    @EventHandler
    public void onPlayerCraftItem(CraftItemEvent event) {
        // 检查事件是否由玩家触发
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack craftedItem = event.getCurrentItem(); // 获取制作的物品
            String craftedItemInfo = (craftedItem != null) ? craftedItem.getType().toString() + " x" + craftedItem.getAmount() : "unknown item";
            String eventInfo = "player " + player.getName() + " crafted " + craftedItemInfo;
            callback.handleEventInfo(player, eventInfo, "event-item");
        }
    }


}
