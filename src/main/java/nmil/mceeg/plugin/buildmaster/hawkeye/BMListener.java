package nmil.mceeg.plugin.buildmaster.hawkeye;
import com.google.gson.Gson;
import nmil.mceeg.plugin.buildmaster.BuildMaster;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterEndEvent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterMsgEvent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BMListener implements Listener {

    private final BuildMaster plugin;
    private final Hawkeye hawkeye;

    public BMListener(BuildMaster plugin, Hawkeye hawkeye) {
        this.plugin = plugin;
        this.hawkeye = hawkeye;
    }
    @EventHandler
    public void onBuildMasterMsgEvent(BuildMasterMsgEvent event) {

        //plugin.getLogger().info("Hawkeye : onBuildMasterMsgEvent");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("JavaTime", sdf.format(new Date()));
        msgWrapper.put("GameTime", event.getPlayer().getWorld().getTime());
        msgWrapper.put("gameState", event.getGameState());
        msgWrapper.put("msg", event.getMessage());

        hawkeye.sendPlayerLog(event.getPlayer(), msgWrapper,"BUILD_MASTER_MSG");
    }
    @EventHandler
    public void onBuildMasterStartEvent(BuildMasterStartEvent event) {

        plugin.getLogger().info("Hawkeye : onBuildMasterStartEvent");


        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", "BUILD_MASTER_START");
        msgWrapper.put("data", event.getPlayer().getName());
        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);
        hawkeye.sendWebSocketMessage(json);

        hawkeye.startCapture(event.getPlayer());
    }
    @EventHandler
    public void onBuildMasterEndEvent(BuildMasterEndEvent event) {

        plugin.getLogger().info("Hawkeye : onBuildMasterEndEvent");


        hawkeye.stopCapture(event.getPlayer());

        Map<String, Object> msgWrapper = new HashMap<>();
        msgWrapper.put("title", "BUILD_MASTER_END");
        msgWrapper.put("data", event.getPlayer().getName());

        if (hawkeye.playerLogCount.containsKey(event.getPlayer())) {
            Long currentValue = hawkeye.playerLogCount.get(event.getPlayer());
            msgWrapper.put("total_log", currentValue);
        }
        hawkeye.playerLogCount.remove(event.getPlayer());

        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);
        hawkeye.sendWebSocketMessage(json);

    }

}