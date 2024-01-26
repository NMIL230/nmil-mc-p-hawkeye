package nmil.mceeg.plugin.buildmaster.hawkeye;
import com.google.gson.Gson;
import nmil.mceeg.plugin.buildmaster.BuildMaster;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterEndEvent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterMsgEvent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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


        Map<String, Object> msgWrapper = new HashMap<>();
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
        Gson gson = new Gson();
        String json = gson.toJson(msgWrapper);
        hawkeye.sendWebSocketMessage(json);
    }

}