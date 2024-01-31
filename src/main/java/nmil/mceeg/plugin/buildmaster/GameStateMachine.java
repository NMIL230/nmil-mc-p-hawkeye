package nmil.mceeg.plugin.buildmaster;

import nmil.mceeg.plugin.buildmaster.event.BuildMasterEndEvent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterMsgEvent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GameStateMachine {
    private final int GS0_PreGame_Countdown = 10; //10s
    private final int GS1_Load_Countdown = 1;
    private final int GS2_Observe_Countdown = 15; //15s
    private final int GS3_Build_Countdown = 30; //30s
    private final int GS4_Judge_Countdown = 6; //6s
    private final int GAME_OVER_Countdown = 10;//10s

    enum GameState {
        GS0_PreGame, GS1_Load, GS2_Observe, GS3_Build, GS4_Judge, GAME_OVER
    }

    enum GameType {
        Classical, Random2D, Random3D
    }



    private GameType currentGameType;

    private BuildMaster plugin;
    private TextDisplay display;
    private  BlockIO blockIO;

    private GameRecordIO gameRecordIO;
    public boolean playerLeft = false;

    public GameStateMachine(BuildMaster plugin) {
        this.plugin = plugin;
        this.display = new TextDisplay();
        this.blockIO = new BlockIO();
        this.gameRecordIO = new GameRecordIO();
        currentGameState = GameState.GAME_OVER;
        currentGameType = GameType.Classical;
    }

    // Current game state
    private GameRecord gameRecord;
    private Player player;
    private int difficulty;
    private GameState currentGameState;
    private int countdown;
    private BukkitTask gameTask;

    private int score;

    private Map<Material, ItemCountsPair> playerItemCounts;

    private String filename;
    private String subDirectory;


    public static class ItemCountsPair{
        int original;
        int current;
        int slot;

        public ItemCountsPair(int original, int current, int slot) {
            this.original = original;
            this.current = current;
            this.slot = slot;
        }
    }

    // Method to start the game
    public void onGame(Player player, int difficulty) {



        this.player = player;
        this.gameRecord = new GameRecord(player.getName());
        this.difficulty = difficulty;
        this.filename = null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        this.subDirectory =  currentGameType + "_" + difficulty + "_" + player.getName() + "_" + timestamp;

        playerLeft = false;
        blockIO.clearArea(plugin.getPlatformCenterLocation());
        display.sendChatToPlayer(player, player.getName() + " joined Build Master, " + currentGameType , "green");
        currentGameState = GameState.GS0_PreGame;
        countdown = GS0_PreGame_Countdown; // 10 seconds countdown before game start
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, this::gameLoop, 20, 20);

        Bukkit.getServer().getPluginManager().callEvent(new BuildMasterStartEvent("",player));
        emitHawkeyeBMEvent("***GAME START***","GS0_PreGame");
    }

    // Game loop method
    private void gameLoop() {
//        display.sendChatToPlayer(player, "gameLoop: "+currentGameState, "aqua");
        switch (currentGameState) {
            case GS0_PreGame:
                handleGS0();
                break;
            case GS1_Load:
                handleGS1();
                break;
            case GS2_Observe:
                handleGS2();
                break;
            case GS3_Build:
                handleGS3();
                break;
            case GS4_Judge:
                judgeGameState(score);
                break;
            case GAME_OVER:
                handleGameOver();
                break;
        }
    }

    // Logic for handling GS0 phase
    private void handleGS0() {
        this.score = -1;
        blockIO.clearArea(plugin.getPlatformCenterLocation());

        if (countdown > 0) {
            if (countdown > 3) {
                display.displayActionBarToPlayer(player, "Game starts in: " + countdown + " seconds", "green");
                //RainBow RanDom
            } else {
                display.displayActionBarToPlayer(player, "Game starts in: " + countdown + " seconds", "red");
            }
            if (countdown == GS0_PreGame_Countdown) {
                if(currentGameType == GameType.Random2D) {
                    display.displayTitleToPlayer(player, "RAINBOW RANDOM", currentGameType + " Difficulty: " + difficulty, "green");
                    //display.sendChatToPlayer(player, "Rainbow Random " + currentGameType, "yellow");
                    filename = Randomization.generateAndSave2DCarpet(plugin.getPlatformCenterLocation(),difficulty,player,subDirectory);        emitHawkeyeBMEvent(player.getName() + " joined Build Master, " + currentGameType ,"GS0_PreGame");
                    emitHawkeyeBMEvent("***GS0_PreGame***","GS0_PreGame");
                    emitHawkeyeBMEvent("RAINBOW RANDOM " + currentGameType + " Level: " + difficulty + ". Game starts in: "  + countdown + " seconds."
                            ,"GS0_PreGame");
                    emitHawkeyeBMEvent(Randomization.getAndClearGlobalBlocksInfo(),"GS0_PreGame");
                }
                else if(currentGameType == GameType.Random3D) {
                    display.displayTitleToPlayer(player, "RAINBOW RANDOM", currentGameType + " Difficulty: " + difficulty, "green");
                    //display.sendChatToPlayer(player, "Rainbow Random " + currentGameType, "yellow");
                    filename = Randomization.generateAndSave3DBlocks(plugin.getPlatformCenterLocation(),difficulty,player,subDirectory);
                    emitHawkeyeBMEvent("***GS0_PreGame***","GS0_PreGame");
                    emitHawkeyeBMEvent("RAINBOW RANDOM " + currentGameType + " Level: " + difficulty + ". Game starts in: "  + countdown + " seconds."
                            ,"GS0_PreGame");
                    emitHawkeyeBMEvent(Randomization.getAndClearGlobalBlocksInfo(),"GS0_PreGame");
                } else {

                    display.displayTitleToPlayer(player, "SPEED BUILDER", currentGameType + " Difficulty: " + difficulty, "green");
                    //display.sendChatToPlayer(player, "Speed Builder " + currentGameType , "yellow");
                    subDirectory = null;
                }
            }
            countdown--;
        } else {

            currentGameState = GameState.GS1_Load; // Transition to GS1 phase after countdown
            countdown = GS1_Load_Countdown; // Set countdown for GS1 phase
        }
    }


    // Logic for handling GS1 phase
    private void handleGS1() {
        if (countdown == GS1_Load_Countdown) {

            gameRecord.addLevelRecord(difficulty);


            display.sendChatToPlayer(player, "Game start!", "yellow");
            player.teleport(plugin.getPlatformSpawnLocation());
            playerLeft = false;
            String fileName = mapDifficultyToFileName(difficulty);
            blockIO.loadStructureFromFile(plugin.getPlatformCenterLocation(), fileName, subDirectory);
            clearPlayerInventory(player);

            emitHawkeyeBMEvent("***GS1_Load***","GS1_Load");
            emitHawkeyeBMEvent("Structure loaded, Player teleported to Spawn Location","GS1_Load");

            countdown--;

        }
        else if (countdown == 0) {
            currentGameState = GameState.GS2_Observe;
            countdown = GS2_Observe_Countdown;
        }
    }



    // Logic for handling GS2 phase
    private void handleGS2() {
        if (countdown == GS2_Observe_Countdown) {
            display.displayTitleToPlayer(player, "Observe", "","green");
            display.sendChatToPlayer(player, "Observe Phase: Please observe the blocks and remember their arrangement.", "yellow");

            emitHawkeyeBMEvent("***GS2_Observe***","GS2_Observe");
            emitHawkeyeBMEvent("Observe Phase: player started observe the blocks and remember their arrangement.","GS2_Observe");

        }
        if (countdown > 0) {
            String color = (countdown <= 3) ? "red" : "green";
            display.displayActionBarToPlayer(player, "Observation phase: " + countdown + " seconds", color);
            countdown--;
        } else {
            blockIO.clearArea(plugin.getPlatformCenterLocation());
            currentGameState = GameState.GS3_Build;
            countdown = GS3_Build_Countdown;
        }
    }


    // Logic for handling GS3 phase
    private void handleGS3() {
        if (countdown == GS3_Build_Countdown) {
            playerItemCounts = blockIO.giveBlocksAndFireworkToPlayer(player, mapDifficultyToFileName(difficulty), subDirectory);

            display.displayTitleToPlayer(player, "Build!", "Submit: Firework", "green");
            display.sendChatToPlayer(player, "Building phase: Try to replicate the blocks from memory. Use firework to submit.", "yellow");

            emitHawkeyeBMEvent("***GS3_Build***","GS3_Build");
            emitHawkeyeBMEvent("Building phase: player try to replicate the blocks from memory. Use firework to submit.","GS3_Build");
        }
        if (countdown > 0) {
            String color = (countdown <= 5) ? "red" : "green";
            display.displayActionBarToPlayer(player, "Building phase: " + countdown + " seconds", color);
            countdown--;
        } else {
            submitBuild();
        }
    }
    void submitBuild() {

        playerItemCounts.clear();
        clearPlayerInventory(player);

        currentGameState = GameState.GS4_Judge;
        countdown = GS4_Judge_Countdown;

        display.sendChatToPlayer(player, "Building Submitted", "yellow");

        emitHawkeyeBMEvent("***GS4_Judge***","GS4_Judge");
        emitHawkeyeBMEvent("Judge phase: Building Submitted, Game is calculating the score, then player will entry the next level.","GS4_Judge");

        this.score = blockIO.judge(plugin.getPlatformCenterLocation(),mapDifficultyToFileName(difficulty),subDirectory);
//        display.sendChatToAllPlayers("submitBuild: score: " + score, "gold");

        gameRecord.updateLevelRecord(difficulty, score >= 80, score);

//        display.sendChatToAllPlayers("handleScore: score: " + score, "light_purple");

        if (score == 100) {
            gameRecord.updateHighestLevel(difficulty);
            display.displayTitleToPlayer(player, "Passed","Perfect",  "blue");
        } else if (score >= 80){
            gameRecord.updateHighestLevel(difficulty);
            display.displayTitleToPlayer(player, "Passed","Score: " + score + "%",  "green");
        } else {
            display.displayTitleToPlayer(player, "Failed","Score: " + score + "%",  "yellow");
        }
    }

    private void judgeGameState(int score) {
        countdown--;
        if (countdown > 3 || score == -1) {
            return;
        }
        if (score >= 80) {

            if (difficulty < 10) {
                difficulty++;
                display.sendChatToPlayer(player, "Judge phase: "+ "Score: " + score +", You passed, Next Level: difficulty " + difficulty, "yellow");
                countdown = 10;
                currentGameState = GameState.GS0_PreGame;
            } else {
                currentGameState = GameState.GAME_OVER;
                countdown = GAME_OVER_Countdown;
            }
        } else {

            List<GameRecord.LevelRecord> records = gameRecord.getLevelRecords().get(difficulty);
            if (records != null) {
                int failedCount = 0;
                for (GameRecord.LevelRecord record : records) {
                    if (!record.isPassed()) {
                        failedCount++;
                    }
                }
                if (failedCount >= 2) {
                    display.sendChatToPlayer(player, "Judge phase: "+ "Score: " + score +", You failed twice on level " + difficulty, "dark_green");
                    currentGameState = GameState.GAME_OVER;
                    countdown = GAME_OVER_Countdown;
                    return;
                }
            } else {
                display.sendChatToAllPlayers("Error: records == null " + difficulty, "red");
            }

            if (difficulty > 1) difficulty--;
            display.sendChatToPlayer(player, "Judge phase: "+ "Score: " + score +", You failed. Returning to the previous level: difficulty " + difficulty, "yellow");
            countdown = GS0_PreGame_Countdown;
            currentGameState = GameState.GS0_PreGame;

        }
    }

    public void handleGameOver() {
        if (countdown == GAME_OVER_Countdown){
            blockIO.clearArea(plugin.getPlatformCenterLocation());
            score = -1;
            clearPlayerInventory(player);
            if (gameRecord != null) {
                if (gameRecord.getHighestLevel() == 10) {
                    display.displayTitleToPlayer(player, "Victor", "Best Score: Difficulty " + gameRecord.getHighestLevel(), "blue");
                    display.sendChatToPlayer(player,player.getName() + " finished Build Master, Best Score: Difficulty " + gameRecord.getHighestLevel(), "green");
                    //tag
                    plugin.playerTag.put(player, "victor");
                } else {
                    display.displayTitleToPlayer(player, "Game Over", "Best Score: Difficulty " + gameRecord.getHighestLevel(), "red");
                    display.sendChatToPlayer(player,player.getName() + " finished Build Master, Best Score: Difficulty " + gameRecord.getHighestLevel(), "green");
                    display.sendChatToPlayer(player,"Game Over", "yellow");

                }

                // record
                gameRecordIO.saveRecordToFile(gameRecord);

                emitHawkeyeBMEvent("***GAME_OVER***","GAME_OVER");
                emitHawkeyeBMEvent("Game Over, Player left.","GAME_OVER");

                Bukkit.getServer().getPluginManager().callEvent(new BuildMasterEndEvent("",player));


            }
        }
        if (countdown == 0){
            display.sendChatToPlayer(player,"Teleporting You to lobby","yellow");
            player.teleport(plugin.getLobbyLocation());
            gameTask.cancel();
        }
        if (countdown < 4) {
            display.displayTitleToPlayer(player, String.valueOf(countdown), "Teleporting", "yellow");
        }
        if (countdown < 7) {
            display.displayActionBarToPlayer(player,"Teleporting You to lobby " +  countdown, "yellow");
        }
        countdown--;
    }

    public void endGameOP() {
        gameTask.cancel();
        currentGameState = GameState.GAME_OVER;
        blockIO.clearArea(plugin.getPlatformCenterLocation());
        if (gameRecord != null) {
            String color =  gameRecord.getHighestLevel() == 10 ? "blue" : "red";
            display.displayTitleToPlayer(player, "Game Over", "Best Score: Difficulty " + gameRecord.getHighestLevel(), color);
            //display.sendChatToAllPlayers(player.getName() + " finished Build Master, Best Score: Difficulty " + gameRecord.getHighestLevel(), "green");
        }
        countdown = 0;
        score = -1;

    }

    public void endGamePlayer() {

        currentGameState = GameState.GAME_OVER;
        countdown = GAME_OVER_Countdown;
    }


    public void onPlayerLeave(Player player, Location center) {
        playerLeft = true;
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {
                    display.displayTitleToPlayer(player, String.valueOf(countdown), "Please return to the game area, ", "Yellow");
                    countdown--;
                    if (isPlayerBackInArea(player.getLocation(), center)) {
                        playerLeft = false;
                        this.cancel();
                    }
                } else {
                    endGamePlayer();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private boolean isPlayerBackInArea(Location player, Location center) {
        double distanceX = Math.abs(player.getX() - center.getX());
        double distanceY = Math.abs(player.getY() - center.getY());
        double distanceZ = Math.abs(player.getZ() - center.getZ());
        return (distanceX <= 12 && distanceZ <= 16 && distanceY <= 13);
    }

    private String mapDifficultyToFileName(int difficulty) {
        if (currentGameType == GameType.Classical) {
            return String.valueOf(difficulty);
        } else if (currentGameType == GameType.Random2D) {
            return filename;
        } else if (currentGameType == GameType.Random3D) {
            return filename;
        }
        return String.valueOf(difficulty);
    }

    public void clearPlayerInventory(Player player) {
        if (player != null) {
            player.getInventory().clear();
//            player.getInventory().setArmorContents(null);
            player.getInventory().setExtraContents(null);
        }
    }

    private void emitHawkeyeBMEvent(String msg, String state) {
        BuildMasterMsgEvent event = new BuildMasterMsgEvent(msg,getPlayer(),state);
        Bukkit.getServer().getPluginManager().callEvent(event);

    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public void setCurrentGameState(GameState currentGameState) {
        this.currentGameState = currentGameState;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }


    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    public Map<Material, ItemCountsPair> getPlayerItemCounts() {
        return playerItemCounts;
    }
    public GameType getCurrentGameType() {
        return currentGameType;
    }

    public void setCurrentGameType(GameType currentGameType) {
        this.currentGameType = currentGameType;
    }
}
