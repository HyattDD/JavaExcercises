import java.io.Serializable;
import java.util.HashMap;

public class Tree implements Serializable{
    private static final long serialVersionUID = 134252411453L;
    private HashMap<String, String> blobMap = new HashMap<>();

    public void getIndex(HashMap<String, String> indexMap) {
        this.blobMap = indexMap;
    }

    public HashMap<String, String> getBlob() {
        return this.blobMap;
    }
}
