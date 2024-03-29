package nmil.mceeg.plugin.buildmaster.hawkeye;

import nmil.mceeg.plugin.buildmaster.BuildMaster;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;

public class ObservationSpaceGetter {
    private final Hawkeye hawkeye;

    public ObservationSpaceGetter( Hawkeye hawkeye) {
        this.hawkeye = hawkeye;
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
               // data.put("Health",(int) player.getHealth());
               // data.put("Hunger", player.getFoodLevel());
                data.put("Location", player.getLocation().toVector());
                data.put("View", getPlayerView(player));
                data.put("TargetBlock",getPlayerTargetBlock(player));
                data.put("RayTrace", getPlayerTargetBlockExactLocation(player));
              //  data.put("TargetEntity",getPlayerTargetEntity(player));
               // data.put("NearbyEntities",getNearbyEntities(player));
                //data.put("Hot-bar",getPlayerHotbar(player));
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
                    //data.put("NearbyBlocks",getNearbyBlocks(player));
                }
                break;
            case "event-item":
                if (event != null) {
                    data.put("Event", event);
                    data.put("Hot-bar",getPlayerHotbar(player));
                    //data.put("Inventory", getSimpleItemStacks(player.getInventory().getContents()));
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
    public Vector getPlayerTargetBlockExactLocation(Player player) {
        World world = player.getWorld();
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        double maxDistance = hawkeye.getMAX_TARGET_DISTANCE();

        // 检查玩家位置是否在世界的加载范围内
        if (!world.isChunkLoaded(eyeLocation.getBlockX() >> 4, eyeLocation.getBlockZ() >> 4)) {
            // 如果玩家所在的区块未加载，可以返回null或是一个默认值
            return null; // 或者 new Vector(0, 0, 0) 作为默认值
        }

        RayTraceResult result = world.rayTraceBlocks(eyeLocation, direction, maxDistance);

        if (result != null && result.getHitBlock() != null) {
            Location hitLocation = result.getHitPosition().toLocation(world);
            return hitLocation.toVector();
        }
        // 如果没有命中任何方块或者其他原因导致结果为null，则返回null或默认值
        return null; // 或者 new Vector(0, 0, 0) 作为默认值
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
                itemData.put("slot", i + 1);
                itemData.put("type", item.getType().toString());
                itemData.put("amount", item.getAmount());
            }
//            else {
//                itemData.put("type", "None");
//                itemData.put("amount", 0);
//            }
            hotbarItems.add(itemData);
        }
        return hotbarItems;
    }
    public Map<String,String> getPlayerTargetBlock(Player player) {
        Map<String,String> block = new HashMap<>();
        Block targetBlock = player.getTargetBlock(null, hawkeye.getMAX_TARGET_DISTANCE());
        if (targetBlock.getType() != Material.AIR) {
            Location blockLocation = (targetBlock).getLocation();
            String locationString = "[x=" + blockLocation.getBlockX() + ", y=" + blockLocation.getBlockY() + ", z=" + blockLocation.getBlockZ()+ "]";
            block.put(targetBlock.getType().toString().toLowerCase(), locationString);
            return block;
        }
        return null;
    }
    public String getPlayerTargetEntity(Player player) {
        List<Entity> nearbyEntities = player.getNearbyEntities(hawkeye.getMAX_TARGET_DISTANCE(), hawkeye.getMAX_TARGET_DISTANCE(), hawkeye.getMAX_TARGET_DISTANCE());
        Vector playerDirection = player.getLocation().getDirection();
        Location playerLocation = player.getEyeLocation();

        for (int i = 1; i <= hawkeye.getMAX_TARGET_DISTANCE(); i++) {
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

    public Map<String, String> getNearbyBlocks(Player player) {
        Map<String,String> blocks = new HashMap<>();
        Location location = player.getLocation();
        int radius = hawkeye.getOBSERVATION_RADIUS();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(location.add(x, y, z));
                    Location blockLocation = block.getLocation();
                    String locationString = "[x=" + blockLocation.getBlockX() + ", y=" + blockLocation.getBlockY() + ", z=" + blockLocation.getBlockZ()+ "]";
                    blocks.put(block.getType().toString().toLowerCase(), locationString);
                }
            }
        }
        return blocks;
    }
    public Set<String> getNearbyEntities(Player player) {
        Set<String> entities = new HashSet<>();
        int radius = hawkeye.getOBSERVATION_RADIUS();
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
