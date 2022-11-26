import java.io.Serializable;

public class Head implements Serializable{
    private static final long serialVersionUID = 111828881453L;
    private String commitId;
    // construct HEAD Object with commitID, CommitId generates from many tree
    // objects's value, and HEAD store the newest commit's commitID
    public Head(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

}
