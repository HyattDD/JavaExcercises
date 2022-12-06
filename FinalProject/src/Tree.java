import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Tree implements Serializable{
    private static final long serialVersionUID = 134252411453L;
    private String treeName;
    // since tree has to store files and dirs, use two map to store them
    // corgit does not store the information of empty dir
    private HashMap<String, String> blobMap = new HashMap<>();
    private HashMap<String, String> treeMap = new HashMap<>();


    public HashMap<String, String> getBlobMap() {
        return blobMap;
    }

    public HashMap<String, String> getTreeMap() {
        return treeMap;
    }

    public String getTreeId(String treeName) {
        return treeMap.get(treeName);
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public String getTreeHash() {
        return MyUtil.getHashOfByteArray(this.toString().getBytes());
    }


    // when the dir has some subfiles in, it's tree stores it by adding to blobMap
    public boolean addBlob(String fileName, String hashText) {
        boolean addSuccess = false;
        blobMap.put(fileName, hashText);
        return addSuccess;
    }

    // when the dir has some subdirs in, it's tree stores it by adding to treeMap
    public boolean addTree(String dirName, String hashText) {
        boolean addSuccess = false;
        treeMap.put(dirName, hashText);
        return addSuccess;
    }

    // get tree object information by string to generate the treeID
    // every time the file and dirs under the tree change, the new treeID will 
    // be different
    @Override 
    public String toString() {
        String content = "";
        Set<String> blobSet = blobMap.keySet();
        Set<String> treeSet = treeMap.keySet();
        String[] blobKeys = blobSet.toArray(new String[blobSet.size()]);
        String[] treeKeys = treeSet.toArray(new String[treeSet.size()]);
        Arrays.sort(blobKeys);
        Arrays.sort(treeKeys);
        for (String key : blobKeys) {
            content += key;
            content += " ";
            content += blobMap.get(key) + '\n';
        }
        for (String key : treeKeys) {
            content += key;
            content += " ";
            content += treeMap.get(key) + '\n';
        }
        return content;
    }

}
