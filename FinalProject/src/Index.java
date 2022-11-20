import java.io.Serializable;
import java.util.HashMap;

public class Index implements Serializable {
    private static final long serialVersionUID = 134252411453L;
    private HashMap<String, String> addMap = new HashMap<>();

    public void set(String keyString, String valueString) {
        this.addMap.put(keyString, valueString);
    }

    public void remove(String keyString) {
        this.addMap.remove(keyString);
    }

    public String getValue(String keyString) {
        return this.addMap.get(keyString);
    }

    @Override 
    public String toString() {
        return "";
    }
}
