import java.io.Serializable;
import java.security.MessageDigest;

public class Blob implements Serializable {
    /*
     * Why use serialVersionUID ?
     * SerialVersionUID is an ID which is stamped on object when it get serialized
     * usually hashcode of object, you can use tool serialver to see
     * serialVersionUID of a serialized object .
     */
    private static final long serialVersionUID = 117822411453L;

    // blob just store the content's value of file content
    private String hashValueOfFileContent;
    private int lengthOfContent;
    // the content in blob is in byte[] content, rather than string
    private byte[] content;

    // consturct a blob just need file's content
    public Blob(byte[] content) {
        this.content = content;
        this.lengthOfContent = content.length;
        this.hashValueOfFileContent = MyUtil.getHashOfByteArray(content);
    }

    // print the content of origin file content
    public void printContent() {
        System.out.println(this.content.toString());
    }

    // get SHA-1 hash value of byte[]
    public static String getHashOfByteArray(byte[] content) {
        String hashValue = "";
        try {
            MessageDigest complete = MessageDigest.getInstance("SHA-1");
            complete.update(content);
            byte[] sha1 = complete.digest();
            // the main algorithm
            for (int j = 0; j < sha1.length; j++) {
                hashValue += Integer.toString((sha1[j] >> 4) & 0x0F, 16)
                        + Integer.toString(sha1[j] & 0x0F, 16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashValue;
    }

    // override the tostring function, so that we can use many blob to generate
    // a tree sha-1 value, and then use the tree-hashvalue to generate commitId
    @Override
    public String toString() {
        return "Blob" + " " + lengthOfContent + " " + hashValueOfFileContent;
    }
}
