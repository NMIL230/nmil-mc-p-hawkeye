package nmil.mceeg.plugin.hawkeye;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;

public class ObservationSpaceGetter {
    private final Hawkeye plugin;

    public ObservationSpaceGetter(Hawkeye plugin) {
        this.plugin = plugin;
    }
    public Map<String, Object> getPlayerObservationSpace(Player player, String type, String event) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Map<String, Object> data = new HashMap<>();
        data.put("Event", null);
        data.put("Time", sdf.format(new Date()));
        data.put("GameTime", player.getWorld().getTime());
        switch (type) {
            case "full":
                if (event != null) {
                    data.put("Event", event);
                    data.put("Health",(int) player.getHealth());
                    data.put("Hunger", player.getFoodLevel());
                    data.put("Location", player.getLocation().toVector());
                    data.put("View", getPlayerView(player));
                    data.put("TargetBlock",getPlayerTargetBlock(player));
                    data.put("TargetEntity",getPlayerTargetEntity(player));
                    data.put("NearbyEntities",getNearbyEntities(player));
                    data.put("Hot-bar",getPlayerHotbar(player));
                    data.put("NearbyBlocks",getNearbyBlocks(player));
                    data.put("Biome",getPlayerBiome(player));
                    data.put("Inventory", getSimpleItemStacks(player.getInventory().getContents()));
                    data.put("Equipments",getPlayerEquipment(player));
                }
                break;
            case "high":
                data.put("Health",(int) player.getHealth());
                data.put("Hunger", player.getFoodLevel());
                data.put("Location", player.getLocation().toVector());
                data.put("View", getPlayerView(player));
                data.put("TargetBlock",getPlayerTargetBlock(player));
                data.put("TargetEntity",getPlayerTargetEntity(player));
                data.put("NearbyEntities",getNearbyEntities(player));
                data.put("Hot-bar",getPlayerHotbar(player));
                break;

            case "low":
                data.put("NearbyBlocks",getNearbyBlocks(player));
                data.put("Biome",getPlayerBiome(player));
                break;


            case "event-interact":
                if (event != null) {
                    data.put("Event", event);
                    data.put("TargetBlock",getPlayerTargetBlock(player));
                    data.put("TargetEntity",getPlayerTargetEntity(player));
                }
                break;
            case "event-block":
                if (event != null) {
                    data.put("Event", event);
                    data.put("TargetBlock",getPlayerTargetBlock(player));
                    data.put("NearbyBlocks",getNearbyBlocks(player));
                }
                break;
            case "event-item":
                if (event != null) {
                    data.put("Event", event);
                    data.put("Hot-bar",getPlayerHotbar(player));
                    data.put("Inventory", getSimpleItemStacks(player.getInventory().getContents()));
                    data.put("Equipments",getPlayerEquipment(player));
                }
                break;
            case "event-fight":
                if (event != null) {
                    data.put("Event", event);
                    data.put("Health",(int) player.getHealth());
                    data.put("Hunger", player.getFoodLevel());
                    data.put("TargetEntity",getPlayerTargetEntity(player));
                    data.put("NearbyEntities",getNearbyEntities(player));
                    data.put("Hot-bar",getPlayerHotbar(player));
                    data.put("Equipments",getPlayerEquipment(player));
                }
                break;
            default:
                data.put("null", "null");
                break;
        }
        return data;
    }

    public Map<String, Float> getPlayerView(Player player) {
        Map<String, Float> view = new HashMap<>();
        Location viewLocation = player.getLocation();
        view.put("yaw", viewLocation.getYaw());
        view.put("pitch", viewLocation.getPitch());
        return view;
    }
    public List<Map<String, Object>> getSimpleItemStacks(ItemStack[] contents) {
        List<Map<String, Object>> simpleItems = new ArrayList<>();
        for (ItemStack item : contents) {
            if (item != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("type", item.getType().toString());
                itemData.put("amount", item.getAmount());
                simpleItems.add(itemData);
            }
        }
        return simpleItems;
    }
    public List<Map<String, Object>> getPlayerHotbar(Player player) {
        List<Map<String, Object>> hotbarItems = new ArrayList<>();
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < 9; i++) {
            ItemStack item = contents[i];
            Map<String, Object> itemData = new HashMap<>();

            if (item != null) {
                itemData.put("type", item.getType().toString());
                itemData.put("amount", item.getAmount());
            } else {
                itemData.put("type", "None");
                itemData.put("amount", 0);
            }
            hotbarItems.add(itemData);
        }
        return hotbarItems;
    }
    public String getPlayerTargetBlock(Player player) {
        Block targetBlock = player.getTargetBlock(null, plugin.getMAX_TARGET_DISTANCE());
        if (targetBlock.getType() != Material.AIR) {
            return targetBlock.getType().toString().toLowerCase();
        }
        return null;
    }
    public String getPlayerTargetEntity(Player player) {
        List<Entity> nearbyEntities = player.getNearbyEntities(plugin.getMAX_TARGET_DISTANCE(), plugin.getMAX_TARGET_DISTANCE(), plugin.getMAX_TARGET_DISTANCE());
        Vector playerDirection = player.getLocation().getDirection();
        Location playerLocation = player.getEyeLocation();

        for (int i = 1; i <= plugin.getMAX_TARGET_DISTANCE(); i++) {
            Location point = playerLocation.add(playerDirection.multiply(i));
//            getLogger().info("Hawkeye: Checking point at " + point);

            for (Entity entity : nearbyEntities) {
                if (entity.getBoundingBox().contains(point.toVector())) {
//                    getLogger().info("Hawkeye: nearbyEntities " + entity.getType().toString().toLowerCase());
                    return entity.getType().toString().toLowerCase();
                }
            }
            playerDirection = player.getLocation().getDirection();
        }
        return null;
    }

    public Set<String> getNearbyBlocks(Player player) {
        Set<String> blocks = new HashSet<>();
        Location location = player.getLocation();
        int radius = plugin.getOBSERVATION_RADIUS();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(location.add(x, y, z));
                    blocks.add(((Block) block).getType().toString().toLowerCase());
                }
            }
        }
        return blocks;
    }
    public Set<String> getNearbyEntities(Player player) {
        Set<String> entities = new HashSet<>();
        int radius = plugin.getOBSERVATION_RADIUS();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            entities.add(entity.getType().toString().toLowerCase());
        }
        return entities;
    }
    public Map<String, String> getPlayerEquipment(Player player) {
        Map<String, String> equipment = new HashMap<>();
        EntityEquipment playerEquipment = player.getEquipment();
        if (playerEquipment != null) {
            if (playerEquipment.getHelmet() != null)
                equipment.put("helmet", playerEquipment.getHelmet().getType().toString().toLowerCase());
            if (playerEquipment.getChestplate() != null)
                equipment.put("chestplate", playerEquipment.getChestplate().getType().toString().toLowerCase());
            if (playerEquipment.getLeggings() != null)
                equipment.put("leggings", playerEquipment.getLeggings().getType().toString().toLowerCase());
            if (playerEquipment.getBoots() != null)
                equipment.put("boots", playerEquipment.getBoots().getType().toString().toLowerCase());
            playerEquipment.getItemInMainHand();
            equipment.put("main_hand", playerEquipment.getItemInMainHand().getType().toString().toLowerCase());
            playerEquipment.getItemInOffHand();
            equipment.put("off_hand", playerEquipment.getItemInOffHand().getType().toString().toLowerCase());
        }
        return equipment;
    }
    public String getPlayerBiome(Player player) {
        Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        return biome.toString().toLowerCase();
    }

}
