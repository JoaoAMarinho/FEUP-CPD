package communication.message;

public class FileTransferMessage extends Message{
    public FileTransferMessage(String key, String value) {
        super();
        this.jsonObject.put("header", "FILETRANSFER")
                .put("key", key)
                .put("value", value);
    }
}
