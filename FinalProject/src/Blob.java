import java.io.Serializable;

public class Blob implements Serializable{
    /* Why use serialVersionUID ?
    SerialVersionUID is an ID which is stamped on object when it get serialized usually hashcode of object, you can use tool serialver to see serialVersionUID of a serialized object . SerialVersionUID is used for version control of object. you can specify serialVersionUID in your class file also. Consequence of not specifying serialVersionUID is that when you add or modify any field in class then already serialized class will not be able to recover because serialVersionUID generated for new class and for old serialized object will be different. Java serialization process relies on correct serialVersionUID for recovering state of serialized object and throws java.io.InvalidClassException in case of serialVersionUID mismatch. 
    */
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
        return "Blob{" + "content=" + new String(content) + ", length=" + length +
                ", hash='" + hash + '\'' + '}';
    }    
}

