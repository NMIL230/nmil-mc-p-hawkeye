package nmil.mceeg.plugin.buildmaster;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nmil.mceeg.plugin.buildmaster.event.BuildMasterMsgEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static java.lang.System.getLogger;

public class TextDisplay {

    public String getColorCode(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "black" -> "§0";
            case "dark_blue" -> "§1";
            case "dark_green" -> "§2";
            case "dark_aqua" -> "§3";
            case "dark_red" -> "§4";
            case "dark_purple" -> "§5";
            case "gold" -> "§6";
            case "gray" -> "§7";
            case "dark_gray" -> "§8";
            case "blue" -> "§9";
            case "green" -> "§a";
            case "aqua" -> "§b";
            case "red" -> "§c";
            case "light_purple" -> "§d";
            case "yellow" -> "§e";
            case "white" -> "§f";
            default -> "§f";
        };
    }

    public void sendChatToAllPlayers(String message, String color) {
        String colorCode = getColorCode(color);
        Bukkit.getServer().broadcastMessage(colorCode + message);
        //emitEvent(message,player);
    }

    public void sendChatToPlayer(Player player, String message, String color) {
        String colorCode = getColorCode(color);
        player.sendMessage(colorCode + message);
        emitEvent(message,player);
    }

    public void displayTitleToPlayer(Player player, String title, String subtitle, String color) {
        String colorCode = getColorCode(color);
        player.sendTitle(colorCode + title, colorCode + subtitle, 10, 30, 20);
    }
    public void displayActionBarToPlayer(Player player, String message, String color) {
        String colorCode = getColorCode(color);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorCode + message));
    }

    private void emitEvent(String msg,Player player) {
        BuildMasterMsgEvent event = new BuildMasterMsgEvent(msg,player);
        Bukkit.getServer().getPluginManager().callEvent(event);

    }
}

