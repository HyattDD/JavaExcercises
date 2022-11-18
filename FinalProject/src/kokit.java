import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class kokit {
    
    public static void main(String[] args) {
        if (args[0].equals("init")) gitInit();
        if (args[0].equals("add")) gitAdd(args[1]);
        if (args[0].equals("commit")) gitCommit(args[1], args[2]);
        if (args[0].equals("--help")) gitHelp();
    }

    // kokit init
    public static boolean gitInit() {
        boolean initSuccess = false;
        // can't use kokit in .kokit directory
        String prePath = System.getProperty("user.dir");
        if (prePath.contains(".kokit")) {
            System.out.println("Kokit can't be used recursively.");
            return initSuccess;
        }
        // initialize when fresh, otherwise reinitialize
        File file = new File(".kokit/objects");
        // if a file dotKokit exists, delete it and renitialized
        File dotKokit = new File(".kokit");
        if (!file.exists() || !file.isDirectory()) {
            dotKokit.delete();
            initSuccess = file.mkdirs();
            if (initSuccess) {
                System.out.println("Initialized empty Kokit repository in " 
                + prePath);
                return initSuccess;
            }
        } else {
            file.delete();
            initSuccess = file.mkdirs();
            if (initSuccess) {
                System.out.println("Reinitialized empty Kokit repository in " 
                + prePath);
                return initSuccess;
            }
        }
        return initSuccess;
    }

    // #TODO
    // kokit add
    public static void gitAdd(String optionString) {
        String path = System.getProperty("user.dir");
        File preFile = new File(path);
        preFile.getParent();
        if (!findFile(optionString, path)) {
            
        }
        if (optionString.equals(".")) {
            // add all
        }

    }


    // #TODO
    // kokit commit (-m "notes")
    public static void gitCommit(String option1, String option2) {
        if (!option1.equals("-m")) {
            System.out.println(option1 + " is not supported by koki-git.");
        }
    }

    // #TODO
    // kokit --help
    public static void gitHelp() {

    }

    // #TODO
    // generate blob object and write in
    public static void generateBlob(String input) throws IOException{
        // 新建初始文件并写入内容
        File file = new File("yourfile");
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
        System.out.println("yourfile has been renamed as: " + hashtext);
    }

    // generate SHA-1 value
    public static String makeSHA1(String input) {
        try {
            // 根据文件内容产生SHA-1值
            /*SHA-1算法以getInstance()的静态方法初始化。选择算法后计算消息摘要值，并将结果作为字节数组返回。 BigInteger类用于将结果字SHA-1为其符号表示。然后将该表示形式转换为十六进制格式，以获取预期的MessageDigest*/
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

    // check whether a file is exited in present path, recursively
    public static Boolean findFile(String fileString, String path) {
        boolean find = false;
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                if (f.getName().equals(fileString)) find = true;
            }
            if (f.isDirectory()) {
                find = findFile(fileString, f.getName() + "/");
            }
        }
        return find;
    }

}
