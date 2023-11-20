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

import java.net.InetSocketAddress;
import java.util.*;

public class Hawkeye extends JavaPlugin implements Listener{
    private int OBSERVATION_RADIUS = 3;
    private int MAX_TARGET_DISTANCE = 5;
    private long UPDATE_RATE = 8L;
    private long DELAY = 0L;

    private Map<Player, BukkitRunnable[]> playerTasks = new HashMap<>();


    private WebSocketServerController wsServerController;
    private ObservationSpaceGetter observationSpaceGetter;
    private BukkitRunnable dataSendTask;
    private CommandController commandController;

    @Override
    public void onEnable() {
        commandController = new CommandController(this);
        observationSpaceGetter = new ObservationSpaceGetter(this);

        EventListenerCallback eventCallback = (player, eventInfo) -> {
            sendPlayerLog(player, observationSpaceGetter.getPlayerObservationSpace(player, "event", eventInfo),"PLAYER_LOG_EVENT");
        };
        PlayerEventListener playerEventListener = new PlayerEventListener(this, eventCallback);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getPluginManager().registerEvents(playerEventListener, this);

        getLogger().info("Hawkeye: onEnable is called!");
        startNewWebSocketServer("Main", new InetSocketAddress("localhost", 8887));
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
        LOW_FREQUENCY_task.runTaskTimer(this, 0L, 20L); // 20 ticks
        HIGH_FREQUENCY_task.runTaskTimer(this, 0L, 1L);  // 1 tick
        playerTasks.put(player, new BukkitRunnable[]{LOW_FREQUENCY_task, HIGH_FREQUENCY_task});
        sendPlayerLogin(player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        getLogger().info("Hawkeye: " + player.getName() + " left the game");

        if (playerTasks.containsKey(player)) {
            BukkitRunnable[] tasks = playerTasks.get(player);
            tasks[0].cancel();
            tasks[1].cancel();
            playerTasks.remove(player);
        }
        sendPlayerLogout(player.getName());
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
    private void sendPlayerLogout(String playerName) {
        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", "SERVER_PLAYER_LOGOUT");
        msgWrapper.put("data", playerName);
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
    public void setUPDATE_RATE(long UPDATE_RATE) {
        this.UPDATE_RATE = UPDATE_RATE;
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
    public long getUPDATE_RATE() {
        return UPDATE_RATE;
    }
    public long getDELAY() {
        return DELAY;
    }
}
