import java.io.File;
import java.util.ArrayList;

public class Log {
    private String originHeadContent;
    private ArrayList<String> commitList = new ArrayList<>();

    Log() {
        String curPath = System.getProperty("user.dir");
        String headContent = MyUtil.showHead(curPath, false);
        this.originHeadContent = headContent;
    }

    // get the commit log, we can choose whther to print out in terminal
    public boolean log(String curPath, boolean printOut) {
        boolean logSuccess = false;
        String headContent = MyUtil.showHead(curPath, false);
        String sep = File.separator;
        String headPath = MyUtil.getHeadFilePath(curPath);
        if (headContent.isEmpty()) {
            return logSuccess;
        } else {
            String objPath = MyUtil.getObjPath(curPath);
            File headFile = new File(headPath);
            File commitFile = new File(objPath + sep + headContent.substring(0, 2) + sep + headContent.substring(2, 40));
            Commit commit = (Commit) MyUtil.readObject(commitFile);
            if (printOut) commit.printLog();
            // modify HEAD
            Head head = MyUtil.readHead(curPath);
            head.setCommitId(commit.getLastCommitId());
            // to ensure that after log, we can get a list of commitIDs
            addToCommitList(commit.getCommitId());
            // write back HEAD
            MyUtil.writeObject(headFile, head);
            log(curPath, printOut);
            logSuccess = true;
        }
        logSuccess = MyUtil.updateHead(originHeadContent);
        return logSuccess;
    }

    public String getOriginHeadContent() {
        return originHeadContent;
    }

    public void addToCommitList(String commitId) {
        this.commitList.add(commitId);
    }

    public ArrayList<String> getCommitList() {
        String curPath = System.getProperty("user.dir");
        log(curPath, false);
        return commitList;
    }

}
