package communication.message;

public class GetMessage extends Message {
    public GetMessage() {
        super();
    }

    public GetMessage(String key) {
        super();
        this.jsonObject.put("header", "GET")
                .put("fileName", key);
    }

    public static GetMessage getResponseMessage(String content) {
        var result = new GetMessage();
        result.jsonObject.put("header", "CONTENT")
                .put("value", content);
        return result;
    }

    public static GetMessage getRedirectMessage(String id) {
        var result = new GetMessage();
        var idInfo = id.split(":");
        result.jsonObject.put("header", "REDIRECT")
                .put("ip", idInfo[0])
                .put("port", idInfo[1]);
        return result;
    }

    public static GetMessage getFileNotFoundMessage() {
        var result = new GetMessage();
        result.jsonObject.put("header", "FILENOTFOUND");
        return result;
    }

    public static GetMessage getFileDeletedMessage() {
        var result = new GetMessage();
        result.jsonObject.put("header", "DELETED");
        return result;
    }
}
