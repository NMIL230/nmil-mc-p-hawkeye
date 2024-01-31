
package nmil.mceeg.plugin.buildmaster;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.*;

public class Randomization {
    private static String globalBlocksInfo = "";

    public static String generateFilename(String baseName, int difficulty, String playerName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        return baseName + "_" + difficulty + "_" + playerName + "_" + timestamp;
    }
    public static String getAndClearGlobalBlocksInfo() {
        String temp = globalBlocksInfo;
        globalBlocksInfo = "";
        return temp;
    }
    public static String generateAndSave2DCarpet(Location center, int difficulty, Player player, String directory) {


        String filename = generateFilename("RR2D", difficulty, player.getName());
        File path = new File("build_master_saves", directory);

        if (!path.exists()) {
            path.mkdirs(); // 创建目录及其所有必要的父目录
        }

        File file = new File(path, filename + ".txt");


        if (difficulty < 7) {
            generateAndSave2DCarpet(center, ((difficulty + 1) / 2) + 1, (difficulty + 2) / 3, difficulty, file);
        } else {
            generateAndSave2DCarpet(center, 4, (difficulty + 2) / 3, difficulty, file);
        }
        return filename;
    }
    public static String generateAndSave3DBlocks(Location center, int difficulty, Player player, String directory) {

        String filename = generateFilename("RR3D", difficulty, player.getName());
        File path = new File("build_master_saves", directory);

        if (!path.exists()) {
            path.mkdirs(); // 创建目录及其所有必要的父目录
        }

        File file = new File(path, filename + ".txt");

        if (difficulty < 7) {
            generateAndSave3DBlocks(center, ((difficulty + 1) / 2) + 1, (difficulty + 2) / 3, difficulty, file);
        } else {
            generateAndSave3DBlocks(center, 4, (difficulty + 2) / 3, difficulty, file);
        }
        return filename;

    }
    public static void generateAndSave3DBlocks(Location center, int size, int colorCount, int blockCount, File file) {
        World world = center.getWorld();
        Random random = new Random();
        List<Material> woolColors = getWoolColors(colorCount);
        Set<Block> blocks = new HashSet<>();
        List<BlockFace> faces = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int colorIndex = 0;

            // Step 1: Generate first block
            assert world != null;
            Block firstBlock = world.getBlockAt(center);
            colorIndex++;
            blocks.add(firstBlock);

            // Write the first block to file
            writer.write(firstBlock.getX() + "," + firstBlock.getY() + "," + firstBlock.getZ() + "," + woolColors.get(0).name() + "\n");

            // Step 2 to 4: Generate other blocks
            for (int i = 1; i < blockCount; i++) {
                Block selectedBlock = new ArrayList<>(blocks).get(random.nextInt(blocks.size()));
                BlockFace face = faces.get(random.nextInt(faces.size()));

                Block adjacentBlock = selectedBlock.getRelative(face);
                while (adjacentBlock.getType() != Material.AIR || blocks.contains(adjacentBlock) || !isBlockWithinBounds(adjacentBlock, center, size)) {
                    face = faces.get(random.nextInt(faces.size()));
                    adjacentBlock = selectedBlock.getRelative(face);
                }

                // Step 3: Set block type
                //adjacentBlock.setType();
                colorIndex++;
                blocks.add(adjacentBlock);

                // Step 4: Check again
                if (areSomeBlocksSurrounded(blocks)) {
                    i--;
                    blocks.remove(adjacentBlock);
                    continue;
                }

                // Step 5: Write to file
                writer.write(adjacentBlock.getX() + "," + adjacentBlock.getY() + "," + adjacentBlock.getZ() + "," + woolColors.get(colorIndex % woolColors.size()).name() + "\n");
                String line = adjacentBlock.getX() + "," + adjacentBlock.getY() + "," + adjacentBlock.getZ() + "," + woolColors.get(colorIndex % woolColors.size()).name();
                globalBlocksInfo += line + ",";

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean areSomeBlocksSurrounded(Set<Block> blocks) {
        for (Block block : blocks) {
            if (isBlockSurrounded(block)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlockSurrounded(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        for (BlockFace face : faces) {
            if (block.getRelative(face).getType() == Material.AIR) {
                return false;
            }
        }

        return true;
    }
    private static boolean isBlockWithinBounds(Block block, Location center, int size) {
        int halfSize = size / 2;
        return Math.abs(block.getX() - center.getBlockX()) <= halfSize &&
                Math.abs(block.getY() - center.getBlockY()) <= size  &&
                Math.abs(block.getZ() - center.getBlockZ()) <= halfSize;
    }



    private static void generateAndSave2DCarpet(Location center, int size, int colorCount, int blockCount, File file) {
        World world = center.getWorld();
        Random random = new Random();
        List<Material> woolColors = getCarpetColors(colorCount);
        Set<Location> usedLocations = new HashSet<>();


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int attempts = 0;
            int maxAttempts = blockCount * 1000;
            int colorIndex = 0;
            for (int i = 0; i < blockCount && attempts < maxAttempts; i++) {
                Material woolColor = woolColors.get(colorIndex % woolColors.size());
                colorIndex++;
                Location randomLocation;
                do {
                    int x = random.nextInt(size) + center.getBlockX() - (size / 2);
                    int z = random.nextInt(size) + center.getBlockZ() - (size / 2);
                    randomLocation = new Location(world, x, center.getBlockY(), z);
                    attempts++;
                } while (usedLocations.contains(randomLocation) && attempts < maxAttempts);

                if (attempts < maxAttempts) {
                    usedLocations.add(randomLocation);
                    writer.write(randomLocation.getBlockX() + "," + randomLocation.getBlockY() + "," + randomLocation.getBlockZ() + "," + woolColor.name() + "\n");
                    String line = randomLocation.getBlockX() + "," + randomLocation.getBlockY() + "," + randomLocation.getBlockZ() + "," + woolColor.name();
                    //writer.write(line + "\n");
                    globalBlocksInfo += line + ",";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Material> getCarpetColors(int colorCount) {
        List<Material> allColors = new ArrayList<>();
        Collections.addAll(allColors,
                Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET, Material.LIGHT_BLUE_CARPET,
                Material.YELLOW_CARPET, Material.LIME_CARPET, Material.PINK_CARPET, Material.GRAY_CARPET,
                Material.LIGHT_GRAY_CARPET, Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET,
                Material.BROWN_CARPET, Material.GREEN_CARPET, Material.RED_CARPET, Material.BLACK_CARPET);

        Collections.shuffle(allColors);

        return allColors.subList(0, Math.min(colorCount, allColors.size()));
    }
    private static List<Material> getWoolColors(int colorCount) {
        List<Material> allColors = new ArrayList<>();
        Collections.addAll(allColors,
                Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
                Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
                Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL);

        Collections.shuffle(allColors);

        return allColors.subList(0, Math.min(colorCount, allColors.size()));
    }
}

