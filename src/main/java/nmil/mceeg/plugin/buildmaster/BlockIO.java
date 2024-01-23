package nmil.mceeg.plugin.buildmaster;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BlockIO {
    private final TextDisplay display;
    private final int size = 9;
    private final File directory;

    public BlockIO() {
        display = new TextDisplay();
        directory = new File("build_master_saves");
        if (!directory.exists()) {
            directory.mkdir();
        }
    }
    public boolean saveStructureToFile(Location center, String fileName) {
        File file = new File(directory, fileName + ".txt");

        World world = center.getWorld();
        if (world == null || size % 2 == 0) {
            display.sendChatToAllPlayers("Save Failed: Size has to be odd","red");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            int baseX = center.getBlockX();
            int baseY = center.getBlockY();
            int baseZ = center.getBlockZ();
            int halfSize = size / 2;

            for (int x = baseX - halfSize; x <= baseX + halfSize; x++) {
                for (int y = baseY; y <= baseY + size - 1; y++) {
                    for (int z = baseZ - halfSize; z <= baseZ + halfSize; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        Material material = block.getType();
                        String line = x + "," + y + "," + z + "," + material.name() + "\n";
                        writer.write(line);
                    }
                }
            }

            clearArea(center);
            return true;

        } catch (IOException e) {
            display.sendChatToAllPlayers("Structure Save Failed: IOException","red");
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadStructureFromFile(Location center, String fileName) {
        File file = new File(directory, fileName + ".txt");
        World world = center.getWorld();

        if (world == null || size % 2 == 0) {
            display.sendChatToAllPlayers("Load Failed: Size has to be odd","red");
            return false;
        }
        clearArea(center);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    Material material = Material.getMaterial(parts[3]);
                    if (material != null) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setType(material);
                    }
                }
            }
            return true;

        } catch (IOException e) {
            display.sendChatToAllPlayers("Structure Load Failed: IOException","red");
            e.printStackTrace();
            return false;
        }
    }
    public void clearArea(Location center) {
        World world = center.getWorld();
        if (world == null) {
            display.sendChatToAllPlayers("Clear Failed: Invalid world", "red");
            return;
        }

        int baseX = center.getBlockX();
        int baseY = center.getBlockY();
        int baseZ = center.getBlockZ();
        int halfSize = (size + 4) / 2;

        for (int x = baseX - halfSize; x <= baseX + halfSize; x++) {
            for (int y = baseY; y <= baseY + size - 1; y++) {
                for (int z = baseZ - halfSize; z <= baseZ + halfSize; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }
    }
    public int judge(Location center, String fileName) {
        int matchCount = 0;
        int totalCount = 0;

        File file = new File(directory, fileName + ".txt");
        World world = center.getWorld();

        if (world == null) {
            return 0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Material originalMaterial = Material.getMaterial(parts[3]);
                    if (originalMaterial != Material.AIR) {
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);

                        Block currentBlock = world.getBlockAt(x, y, z);
                        if (currentBlock.getType() == originalMaterial) {
                            matchCount++;
                        }
                        totalCount++;

//                        display.sendChatToAllPlayers("originalMaterial location " +  x+" "+ y+" " + z, "dark_aqua");
//                        display.sendChatToAllPlayers("currentBlock location " +  currentBlock.getLocation(), "dark_aqua");
//                        display.sendChatToAllPlayers("currentBlock " +  currentBlock.getType(), "dark_aqua");
//                        display.sendChatToAllPlayers("originalMaterial " +  originalMaterial, "dark_aqua");
//
//                        display.sendChatToAllPlayers("judge called, totalCount: " +  totalCount, "dark_aqua");
//                        display.sendChatToAllPlayers("judge called, matchCount: " +  matchCount, "dark_aqua");

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalCount > 0 ? (matchCount * 100 / totalCount) : 0;
    }
    public Map<Material, GameStateMachine.ItemCountsPair> giveBlocksAndFireworkToPlayer(Player player, String fileName) {
        File file = new File(directory, fileName + ".txt");
        Map<Material, GameStateMachine.ItemCountsPair> ItemCounts = new HashMap<>();

        if (!file.exists()) {
            display.sendChatToAllPlayers("File not found: " + fileName, "red");
            return ItemCounts;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int slot = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Material material = Material.getMaterial(parts[3]);
                    if (material != null && material != Material.AIR) {
                        if (ItemCounts.containsKey(material)) {
                            GameStateMachine.ItemCountsPair pair = ItemCounts.get(material);
                            pair.current++;
                            pair.original++;
                            ItemCounts.put(material, pair);
                        } else {
                            ItemCounts.put(material, new GameStateMachine.ItemCountsPair(1,1,slot++));
                        }
                    }
                }
            }
            ItemCounts.put(Material.FIREWORK_ROCKET, new GameStateMachine.ItemCountsPair(1,1,8));

            for (Map.Entry<Material, GameStateMachine.ItemCountsPair> entry : ItemCounts.entrySet()) {
                player.getInventory().setItem(entry.getValue().slot ,new ItemStack(entry.getKey(), entry.getValue().original));
            }


        } catch (IOException e) {
            display.sendChatToPlayer(player, "Error reading file: " + fileName, "red");
            e.printStackTrace();
        }

        return ItemCounts;
    }



}