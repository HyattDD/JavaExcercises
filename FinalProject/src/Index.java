import java.io.Serializable;
import java.util.HashMap;

public class Index implements Serializable {
    private static final long serialVersionUID = 134252411453L;
    private HashMap<String, String> addMap = new HashMap<>();
    private String content = "";

    public void set(String keyString, String valueString) {
        this.addMap.put(keyString, valueString);
        content += valueString;
    }

    public void remove(String keyString) {
        this.addMap.remove(keyString);
        content.replace(getValue(keyString), "");
    }

    public String getValue(String keyString) {
        return this.addMap.get(keyString);
    }

    public String getContent() {
        return this.content;
    }

    public HashMap<String, String> getMap() {
        return this.addMap;
    }


    @Override 
    public String toString() {
        return "";
    }
}
