import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Reset {
    private String commitId;

    Reset(String commitId) {
        this.commitId = commitId;
    }

    public boolean resetSoft(String curPath) {
        boolean reset = false;
        Log log = new Log();
        ArrayList<String> commitList = log.getCommitList();
        // if no commit history, do nothing
        if (commitList.isEmpty()) {
            System.out.println("fatal: no commit history, nothing to reset");
            return reset;
        } 
        // if wrong commitId, do not reset
        if (!commitList.contains(commitId)) {
            System.out.println("wrong commitId, fail to reset");
            return reset;
        }
        // correct commitId, then reset HEAD content
        File headFile = new File(MyUtil.getHeadFilePath(curPath));
        Head head = (Head) MyUtil.readObject(headFile);
        head.setCommitId(commitId);
        MyUtil.writeObject(headFile, head);
        System.out.println("HEAD changed to " + commitId);
        reset = true;
        return reset;
    }

    public boolean resetMixed(String curPath) {
        boolean reset = false;
        // reset soft
        if (!resetSoft(curPath)) return reset;
        // read index and clear it
        String indexPath = MyUtil.getIndexFilePath(curPath);
        File indexFile = new File(indexPath);
        Index index = (Index) MyUtil.readObject(indexFile);
        index.clearIndexMap();

        // get the commitMap
        HashMap<String, String> commitMap = new HashMap<>();
        commitMap = MyUtil.readCommit(commitId);
        Set<String> set = commitMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        
        for (String key : keys) {
            index.setItem(key, commitMap.get(key));
            // System.out.println([Debug] key + " " + commitMap.get(key));
        }
        MyUtil.writeObject(indexFile, index);
        System.out.println("Index recovered to " + commitId);
        reset = true;
        return reset;
    }

    public boolean resetHard(String curPath) {
        boolean reset = false;
        // reset Mixed
        if (!resetMixed(curPath)) return reset;
        // clear working space
        if (!MyUtil.clearWorkingSpace(curPath)) return reset;
        // read index and get it's map
        String indexPath = MyUtil.getIndexFilePath(curPath);
        File indexFile = new File(indexPath);
        Index index = (Index) MyUtil.readObject(indexFile);
        HashMap<String, String> indexMap = index.getMap();
        // get all the file's name (relative path) in index 
        Set<String> set = indexMap.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        String workingSpacePath = MyUtil.getRootPathOfCorigitRepo(curPath);
        String sep = File.separator;
        for (String key : keys) {
            File file = new File(workingSpacePath + sep + key);
            MyUtil.createFile(file);
            Blob blob = MyUtil.readBlob(indexMap.get(key));
            MyUtil.writeBytesToFile(blob.getContent(), file);
        }
        return reset; 
    }

    public boolean headIsEmpty(String curPath) {
        File headFile = new File(MyUtil.getHeadFilePath(curPath));
        Head head = (Head) MyUtil.readObject(headFile);
        if (head.getCommitId().isEmpty()) return true;
        return false;
    }
}
