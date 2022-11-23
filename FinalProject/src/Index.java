import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class Index implements Serializable {

    private static final long serialVersionUID = 134252411453L;
    private HashMap<String, String> indexMap = new HashMap<>();
    private String content = "";

    public void setItem(String keyString, String valueString) {
        this.indexMap.put(keyString, valueString);
        content += valueString;
    }

    public void removeItem(String keyString) {
        this.indexMap.remove(keyString);
        content.replace(getValue(keyString), "");
    }

    public String getValue(String keyString) {
        return this.indexMap.get(keyString);
    }

    public String getContent() {
        return this.content;
    }

    public HashMap<String, String> getMap() {
        return this.indexMap;
    }

    public void listItems() {
        Set<String> set = indexMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        for (String key : keys) {
            System.out.println(key + ": " + getValue(key));
        }

    }


    @Override 
    public String toString() {
        String content = "";
        Set<String> set = indexMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        for (String key : keys) {
            content += key;
            content += indexMap.get(key);
        }
        return content;
    }
}
