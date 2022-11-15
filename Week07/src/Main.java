import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {
        String testString01 = "Hello world";
        String testString02 = "Hello PKU";
        String testString03 = "Hello THU";

        try {
            System.out.println("-----------------------------");
            writeTest01(testString01);
            System.out.println("-----------------------------");
            writeTest02(testString02);
            System.out.println("-----------------------------");
            writeTest03(testString03);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTest01(String input) throws IOException {
        // 新建初始文件并写入内容
        File file = new File("./file01");
        System.out.println("file01 has been made...");
        FileWriter writer = new FileWriter(file);
        writer.write(input);
        System.out.println("testString01: <" + input + "> has been written to file01...");
        
        // 存储SHA-1值
        String hashtext = makeSHA1(input);

        // 修改文件名为SHA-1值
        File renamedFile = new File("./" + hashtext);
        file.renameTo(renamedFile);
        System.out.println("file01 has been renamed as: " + hashtext);
        writer.close();
    }

    public static void writeTest02(String input) throws IOException {

        // 新建初始文件并写入内容
        File file = new File("./file02");
        System.out.println("file02 has been made...");
        try (
            DataOutputStream output = new DataOutputStream(new FileOutputStream(file))
        ) {
            output.writeUTF(input);
            System.out.println("testString02: <" + input + "> has been written to file02...");
        }

        // 存储SHA-1值
        String hashtext = makeSHA1(input);

        // 修改文件名为SHA-1值
        File renamedFile = new File("./" + hashtext);
        file.renameTo(renamedFile);
        System.out.println("file02 has been renamed as: " + hashtext);
    }

    public static void writeTest03(String input) throws IOException{
        // 新建初始文件并写入内容
        File file = new File("./file03");
        System.out.println("file03 has been made...");
        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(file))
        ) {
            Blob blob = new Blob(input);
            blob.setType("String");
            blob.setSize(input.length());
            blob.setContent(makeSHA1(input));
            output.writeObject(blob);
            System.out.println("Object blob has been written to file03");
        }

        // 存储SHA-1值
        String hashtext = makeSHA1(input);

        // 修改文件名为SHA-1值
        File renamedFile = new File("./" + hashtext);
        file.renameTo(renamedFile);
        System.out.println("file03 has been renamed as: " + hashtext);
    }

    // SHA-1值产生器
    public static String makeSHA1(String input) {
        try {
            // 根据文件内容产生SHA-1值
            /*SHA-1算法以getInstance()的静态方法初始化。选择算法后计算消息摘要值，并将结果作为字节数组返回。 BigInteger类用于将结果字SHA-1为其符号表示。然后将该表示形式转换为十六进制格式，以获取预期的MessageDigest。 */
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // getInstance方法中是声明了NoSuchAlgorithmException异常，所以这里要捕获进行处理
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

// Blob 类的实现
class Blob implements Serializable{
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

