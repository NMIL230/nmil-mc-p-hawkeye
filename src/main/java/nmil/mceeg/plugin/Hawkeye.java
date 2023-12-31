package nmil.mceeg.plugin;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;

public class Hawkeye extends JavaPlugin implements Listener{
    private int OBSERVATION_RADIUS = 3;
    private int MAX_TARGET_DISTANCE = 5;

    private Long LOW_UPDATE_RATE = 20L;

    private Long HIGH_UPDATE_RATE = 1L;

    private long DELAY = 0L;

    private Map<Player, BukkitRunnable[]> playerTasks = new HashMap<>();
    private Map<Player, Long> playerLogCount = new HashMap<>();
    //private long playerLogCount = 0L;

    private WebSocketServerController wsServerController;
    private ObservationSpaceGetter observationSpaceGetter;
    private BukkitRunnable dataSendTask;
    private CommandController commandController;

    @Override
    public void onEnable() {
        commandController = new CommandController(this);
        observationSpaceGetter = new ObservationSpaceGetter(this);

        EventListenerCallback eventCallback = (player, eventInfo, type) -> {
            sendPlayerLog(player, observationSpaceGetter.getPlayerObservationSpace(player, type, eventInfo),"PLAYER_LOG_EVENT");
        };
        PlayerEventListener playerEventListener = new PlayerEventListener(this, eventCallback);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getPluginManager().registerEvents(playerEventListener, this);

        getLogger().info("Hawkeye: onEnable is called!");
        startNewWebSocketServer("Main", new InetSocketAddress("localhost", 8887));
        startServerPerformanceTask();
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

    }

    private void startServerPerformanceTask() {
        dataSendTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendServerPerformanceData();
            }
        };
        dataSendTask.runTaskTimer(this, 0L, LOW_UPDATE_RATE);
    }

    private void sendServerPerformanceData()  {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        int onlinePlayers = Bukkit.getServer().getOnlinePlayers().size();
        double serverTick = Lag.getTPS();

        Collection<? extends Player> players = getServer().getOnlinePlayers();
        int averagePing = 0;
        if (!players.isEmpty()) {
            int totalPing = 0;
            for (Player player : players) {
                totalPing += player.getPing();
            }

            averagePing = totalPing / players.size();
        }
        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", "SERVER_STATUS");

        Map<String, Object> performanceData = new HashMap<>();
        performanceData.put("usedMemory", usedMemory);
        performanceData.put("onlinePlayers", onlinePlayers);
        performanceData.put("serverTick", serverTick);
        performanceData.put("averagePing", averagePing);
        msgWrapper.put("data", performanceData);

        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);

//        getLogger().info(json);

        sendWebSocketMessage(json);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getLogger().info("Hawkeye: " + player.getName() + " joined the game, start sending log...");
        BukkitRunnable LOW_FREQUENCY_task = new BukkitRunnable() {
            @Override
            public void run() {
                sendPlayerLog(player, observationSpaceGetter.getPlayerObservationSpace(player, "low",null), "PLAYER_LOG_LOW_FREQUENCY");
            }
        };
        BukkitRunnable HIGH_FREQUENCY_task = new BukkitRunnable() {
            @Override
            public void run() {
                sendPlayerLog(player, observationSpaceGetter.getPlayerObservationSpace(player, "high",null), "PLAYER_LOG_HIGH_FREQUENCY");
            }
        };
        LOW_FREQUENCY_task.runTaskTimer(this, 0L, LOW_UPDATE_RATE); // 20 ticks
        HIGH_FREQUENCY_task.runTaskTimer(this, 0L, HIGH_UPDATE_RATE);  // 1 tick
        playerTasks.put(player, new BukkitRunnable[]{LOW_FREQUENCY_task, HIGH_FREQUENCY_task});
        playerLogCount.put(player, 0L);
        sendPlayerLogin(player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sendPlayerLogout(player);
        Long currentValue = 0L;
        if (playerLogCount.containsKey(player)) {
            currentValue = playerLogCount.get(player);
            playerLogCount.remove(player);
        }
        getLogger().info("Hawkeye: " + player.getName() + " left the game, sent " + currentValue + "Logs");

        if (playerTasks.containsKey(player)) {
            BukkitRunnable[] tasks = playerTasks.get(player);
            tasks[0].cancel();
            tasks[1].cancel();
            playerTasks.remove(player);
        }
    }
    private void startNewWebSocketServer(String usage, InetSocketAddress address) {
        wsServerController = new WebSocketServerController(usage, this, address );
        wsServerController.startServer();
    }
    private void sendPlayerLog(Player player, Map<String, Object> data, String type) {
        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", type);

        Map<String, Object> playerWrapper = new HashMap<>();
        playerWrapper.put("player", player.getName());
        playerWrapper.put("log", data);

        if (playerLogCount.containsKey(player)) {
            Long currentValue = playerLogCount.get(player);
            currentValue++;
            playerLogCount.put(player, currentValue);
        }
        //playerWrapper.put("total_count", currentValue);

        msgWrapper.put("data", playerWrapper);

        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);

        sendWebSocketMessage(json);
    }

    private void sendPlayerLogin(String playerName) {
        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", "SERVER_PLAYER_LOGIN");
        msgWrapper.put("data", playerName);
        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);
        sendWebSocketMessage(json);
    }
    private void sendPlayerLogout(Player player) {
        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", "SERVER_PLAYER_LOGOUT");
        msgWrapper.put("data", player.getName());
        if (playerLogCount.containsKey(player)) {
            Long currentValue = playerLogCount.get(player);
            msgWrapper.put("total_log", currentValue);
        }
        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);
        sendWebSocketMessage(json);
    }




    private void sendWebSocketMessage(String json) {
        wsServerController.broadcast(json);
        //getLogger().info("Hawkeye: " +  json);
    }

    @Override
    public void onDisable() {
        getLogger().info("Hawkeye: onDisable is called!");
        if (wsServerController != null) {
            try {
                wsServerController.stop();
            } catch (InterruptedException e) {
                getLogger().warning("Interrupted while stopping the WebSocket server: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("hawkeye")) {
            return commandController.onCommand(sender, label, args);
        }
        return false;
    }
    public void setOBSERVATION_RADIUS(int OBSERVATION_RADIUS) {
        this.OBSERVATION_RADIUS = OBSERVATION_RADIUS;
    }
    public void setMAX_TARGET_DISTANCE(int MAX_TARGET_DISTANCE) {
        this.MAX_TARGET_DISTANCE = MAX_TARGET_DISTANCE;
    }

    public void setDELAY(long DELAY) {
        this.DELAY = DELAY;
    }
    public int getOBSERVATION_RADIUS() {
        return OBSERVATION_RADIUS;
    }
    public int getMAX_TARGET_DISTANCE() {
        return MAX_TARGET_DISTANCE;
    }
    public long getLOW_UPDATE_RATE() {
        return LOW_UPDATE_RATE;
    }

    public void setLOW_UPDATE_RATE(long LOW_UPDATE_RATE) {
        this.LOW_UPDATE_RATE = LOW_UPDATE_RATE;
    }

    public long getHIGH_UPDATE_RATE() {
        return HIGH_UPDATE_RATE;
    }

    public void setHIGH_UPDATE_RATE(long HIGH_UPDATE_RATE) {
        this.HIGH_UPDATE_RATE = HIGH_UPDATE_RATE;
    }
    public long getDELAY() {
        return DELAY;
    }
}
