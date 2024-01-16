package nmil.mceeg.plugin.hawkeye;
import org.bukkit.entity.Player;

public interface EventListenerCallback {
    void handleEventInfo(Player player, String eventInfo, String type);
}
