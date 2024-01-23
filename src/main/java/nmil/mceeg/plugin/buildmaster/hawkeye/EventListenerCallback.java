package nmil.mceeg.plugin.buildmaster.hawkeye;
import org.bukkit.entity.Player;

public interface EventListenerCallback {
    void handleEventInfo(Player player, String eventInfo, String type);
}
