import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class Index implements Serializable {

    private static final long serialVersionUID = 134252411443L;
    private HashMap<String, String> indexMap = new HashMap<>();

    public boolean isEmpty() {
        return indexMap.isEmpty();
    }

    public void setItem(String keyString, String valueString) {
        this.indexMap.put(keyString, valueString);
    }

    public boolean removeItem(String keyString) {
        boolean remove = false;
        remove = indexMap.containsKey(keyString);
        this.indexMap.remove(keyString);
        return remove;
    }

    public boolean removeDir() {
        boolean remove = false;
        return remove;
    }

    public String getValue(String keyString) {
        return this.indexMap.get(keyString);
    }



    public HashMap<String, String> getMap() {
        return this.indexMap;
    }

    public String[] getKeys() {
        Set<String> set = indexMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        return keys; 
    }

    public int getKeysNumber() {
        return getKeys().length;
    }

    public void listItems() {
        if (indexMap.isEmpty()) {
            System.out.println("Nothing in index");
        } else {
            Set<String> set = indexMap.keySet();
            String[] keys = set.toArray(new String[set.size()]);
            for (String key : keys) {
                System.out.println(getValue(key) + " : " + key);
            }
            System.out.println("Done: list items of index");
        }
    }

    // check whether an Item in index file
    public boolean findItem(String keyString) {
        boolean find = false;
        find = indexMap.containsKey(keyString);
        return find;
    }

    // check whther an item contians a fileName
    public boolean containFileName(String fileName) {
        boolean contain = false;
        Set<String> set = indexMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        for (String key : keys) {
            if (key.contains(fileName)) {
                contain = true;
            }
        }
        return contain;
    }

    // count the number of a name exsits in index file
    public int getAppearedTimes(String fileName) {
        int contain = 0;
        Set<String> set = indexMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        for (String key : keys) {
            if (key.contains(fileName)) {
                contain += 1;
            }
        }
        return contain;
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
