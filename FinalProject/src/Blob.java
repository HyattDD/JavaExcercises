import java.io.Serializable;

public class Blob implements Serializable{
    private static final long serialVersionUID = 134252411453L;

    private String hash;
    private int length;
    private byte[] content;

    public Blob(byte[] content, int length, String hash) {
        this.content = content;
        this.length = length;
        this.hash = hash;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Blob{" +
                "content=" + new String(content) +
                ", length=" + length +
                ", hash='" + hash + '\'' +
                '}';
    }    
}

