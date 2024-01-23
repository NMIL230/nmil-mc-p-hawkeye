package nmil.mceeg.plugin.buildmaster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameRecordIO {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final File directory;

    public GameRecordIO() {
        this.directory = new File("build_master_game_record");
        if (!directory.exists()) {
            directory.mkdir();
        }
    }


    // 方法将GameRecord保存为JSON文件
    public void saveRecordToFile(GameRecord record) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(record);

        String timestamp = dateFormat.format(new Date());
        String fileName = "bm_" + record.getPlayerName() + "_" + timestamp + ".json";
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
