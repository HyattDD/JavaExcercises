import java.io.Serializable;

public class Blob implements Serializable{
    private String type;
    private int size;
    private String content;

    public Blob(String info) {
        this.content = info;
        this.type = "String";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

