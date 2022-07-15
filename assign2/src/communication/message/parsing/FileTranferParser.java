package communication.message.parsing;
import org.json.JSONObject;
import store.Store;
import utils.FileUtils;
import java.io.IOException;

public class FileTranferParser extends MessageParser{
    public FileTranferParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        String key = (String) msgInfo.get("key");

        var filePath = store.getStoragePath() + key + ".txt";
        try {
            FileUtils.CreateFile(filePath, (String) msgInfo.get("value"));
        } catch (IOException e) {
            System.out.println("Could not write to file.");
            e.printStackTrace();
        }
    }
}
