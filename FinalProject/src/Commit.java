import java.io.Serializable;

public class Commit implements Serializable{
    private String lastCommitId;
    private String treeId;
    private String message;
    private String time;

    public Commit(String lastCommitId, String treeId, String message, String time) {
        this.lastCommitId = lastCommitId;
        this.treeId = treeId;
        this.message = message;
        this.time = time;
    }

    @Override
    public String toString() {
        return this.lastCommitId + this.treeId +this.message + this.time;
    }

    
}
