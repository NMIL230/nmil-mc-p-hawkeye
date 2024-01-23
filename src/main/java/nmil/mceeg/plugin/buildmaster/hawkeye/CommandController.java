package nmil.mceeg.plugin.buildmaster.hawkeye;

import org.bukkit.command.CommandSender;

public class CommandController {

    private final Hawkeye plugin;

    public CommandController(Hawkeye plugin) {
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, String label, String[] args) {

//            if (args.length == 2) {
//                try {
//                    switch (args[0].toLowerCase()) {
//                        case "or":
//                            plugin.setOBSERVATION_RADIUS(Integer.parseInt(args[1]));
//                            sender.sendMessage("OBSERVATION_RADIUS set to " + Integer.parseInt(args[1]));
//                            break;
//                        case "mtd":
//                            plugin.setMAX_TARGET_DISTANCE(Integer.parseInt(args[1]));
//                            sender.sendMessage("MAX_TARGET_DISTANCE set to " + Integer.parseInt(args[1]));
//                            break;
//                        case "hur":
//                            plugin.setHIGH_UPDATE_RATE(Long.parseLong(args[1]));
//                            sender.sendMessage("HIGH_UPDATE_RATE set to " + Long.parseLong(args[1]));
//                            break;
//                        case "lur":
//                            plugin.setLOW_UPDATE_RATE(Long.parseLong(args[1]));
//                            sender.sendMessage("LOW_UPDATE_RATE set to " + Long.parseLong(args[1]));
//                            break;
//                        default:
//                            sender.sendMessage("Invalid configuration option");
//                    }
//                } catch (NumberFormatException e) {
//                    sender.sendMessage("Invalid number format");
//                }
//            } else {
//                sender.sendMessage("Usage: /hawkeyeconfig <option> <value>");
//            }
            return true;
    }
}
