package nmil.mceeg.plugin.buildmaster.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BuildMasterMsgEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String message;
    private final Player player;

    public String getGameState() {
        return gameState;
    }

    private final String gameState;


    public BuildMasterMsgEvent(String message, Player player, String gameState) {
        this.gameState = gameState;
        this.message = message;
        this.player = player;
    }

    public String getMessage() {
        return message;
    }
    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
