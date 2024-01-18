package nmil.mceeg.plugin.buildmaster;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class GameStateMachine {


    private int GS0_PreGame_Countdown = 10;
    private int GS1_Load_Countdown = 1;
    private int GS2_Observe_Countdown = 3;
    private int GS3_Build_Countdown = 10;
    private int GS4_Judge_Countdown = 6;
    private int GAME_OVER_Countdown = 10;

    enum GameState {
        GS0_PreGame, GS1_Load, GS2_Observe, GS3_Build, GS4_Judge, GAME_OVER
    }
    private BuildMaster plugin;
    private TextDisplay display;
    private  BlockIO blockIO;

    public GameStateMachine(BuildMaster plugin) {
        this.plugin = plugin;
        this.display = new TextDisplay();
        this.blockIO = new BlockIO();
        currentGameState = GameState.GAME_OVER;
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

    public static class ItemCountsPair{
        int original;
        int current;

        public ItemCountsPair(int original, int current) {
            this.original = original;
            this.current = current;
        }
    }

    // Method to start the game
    public void onGame(Player player, int difficulty) {
        this.player = player;
        this.gameRecord = new GameRecord(player.getName());
        this.difficulty = difficulty;
        blockIO.clearArea(plugin.getPlatformCenterLocation());

        display.sendChatToAllPlayers(player.getName() + " joined Build Master, Level: " + difficulty, "green");

        currentGameState = GameState.GS0_PreGame;
        countdown = GS0_PreGame_Countdown; // 10 seconds countdown before game start
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, this::gameLoop, 20, 20);

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
            } else {
                display.displayActionBarToPlayer(player, "Game starts in: " + countdown + " seconds", "red");
            }
            if (countdown == GS0_PreGame_Countdown) {
                display.displayTitleToPlayer(player, "BUILD MASTER", "Difficulty: " + difficulty, "green");
                display.sendChatToPlayer(player, "Difficult: " + difficulty + " Game starts in " + countdown + " seconds", "yellow");
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
            display.sendChatToPlayer(player, "Game start!", "yellow");
            player.teleport(plugin.getPlatformSpawnLocation());
            String fileName = mapDifficultyToFileName(difficulty);
            blockIO.loadStructureFromFile(plugin.getPlatformCenterLocation(), fileName);
            gameRecord.addLevelRecord(difficulty);
            clearPlayerInventory(player);
        }
        else if (countdown == 0) {
            currentGameState = GameState.GS2_Observe;
            countdown = GS2_Observe_Countdown;
        }
        countdown--;
    }



    // Logic for handling GS2 phase
    private void handleGS2() {
        if (countdown == GS2_Observe_Countdown) {
            display.displayTitleToPlayer(player, "Observe", "","green");
            display.sendChatToPlayer(player, "Please observe the blocks and remember their arrangement.", "yellow");
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
            playerItemCounts = blockIO.giveBlocksAndFireworkToPlayer(player, mapDifficultyToFileName(difficulty));
            display.displayTitleToPlayer(player, "Build!", "Submit: Firework", "green");
            display.sendChatToPlayer(player, "Building phase: try to replicate the blocks from memory. Use firework to submit.", "yellow");
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

        currentGameState = GameState.GS4_Judge;
        countdown = GS4_Judge_Countdown;

        display.sendChatToPlayer(player, "submitBuild: currentGameState: " + currentGameState, "gold");
        this.score = blockIO.judge(plugin.getPlatformCenterLocation(),mapDifficultyToFileName(difficulty));
//        display.sendChatToAllPlayers("submitBuild: score: " + score, "gold");

        gameRecord.updateHighestLevel(difficulty);
        gameRecord.updateLevelRecord(difficulty, score >= 80, score);

//        display.sendChatToAllPlayers("handleScore: score: " + score, "light_purple");

        if (score == 100) {
            display.displayTitleToPlayer(player, "Passed","Perfect",  "blue");
        } else if (score >= 80){
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
                display.sendChatToPlayer(player, "You passed, Next Level: difficulty " + difficulty, "yellow");
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
                    display.sendChatToPlayer(player, "You failed twice on level " + difficulty, "dark_green");
                    currentGameState = GameState.GAME_OVER;
                    countdown = GAME_OVER_Countdown;
                    return;
                }
            } else {
                display.sendChatToAllPlayers("Error: records == null " + difficulty, "red");
            }

            if (difficulty > 1) difficulty--;
            display.sendChatToPlayer(player, "You failed. Returning to the previous level: difficulty " + difficulty, "yellow");
            countdown = GS0_PreGame_Countdown;
            currentGameState = GameState.GS0_PreGame;

        }
    }

    public void handleGameOver() {
        if (countdown == GAME_OVER_Countdown){
            blockIO.clearArea(plugin.getPlatformCenterLocation());
            score = -1;
            if (gameRecord != null) {
                String color =  gameRecord.getHighestLevel() == 10 ? "blue" : "red";
                display.displayTitleToPlayer(player, "Game Over", "Best Score: Difficulty " + gameRecord.getHighestLevel(), color);
                display.sendChatToAllPlayers(player.getName() + " finished Build Master, Best Score: Difficulty " + gameRecord.getHighestLevel(), "green");
            }
        }
        if (countdown == 0){
            display.sendChatToPlayer(player,"Teleporting You to lobby","gold");
            player.teleport(plugin.getLobbyLocation());
            gameTask.cancel();
        }
    }

    public void endGameOP() {
        gameTask.cancel();
        currentGameState = GameState.GAME_OVER;
        blockIO.clearArea(plugin.getPlatformCenterLocation());
        if (gameRecord != null) {
            String color =  gameRecord.getHighestLevel() == 10 ? "blue" : "red";
            display.displayTitleToPlayer(player, "Game Over", "Best Score: Difficulty " + gameRecord.getHighestLevel(), color);
            display.sendChatToAllPlayers(player.getName() + " finished Build Master, Best Score: Difficulty " + gameRecord.getHighestLevel(), "green");
        }
        countdown = 0;
        score = -1;

    }

    public void endGamePlayer() {
        currentGameState = GameState.GAME_OVER;
        countdown = GAME_OVER_Countdown;
    }

    public boolean playerLeft = false;

    public void onPlayerLeave(Player player, Location center) {
        playerLeft = true;
        new BukkitRunnable() {
            int countdown = 10;

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
                    handleGameOver();
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
        return String.valueOf(difficulty);
    }

    public void clearPlayerInventory(Player player) {
        if (player != null) {
            player.getInventory().clear();
//            player.getInventory().setArmorContents(null);
            player.getInventory().setExtraContents(null);
        }
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
}
