import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;


public class kokit {
    
    public static void main(String[] args) {
        if (args[0].equals("init")) gitInit();
        if (args[0].equals("add"))  {
            if (args[1].equals(".")) gitAddAll(System.getProperty("user.dir"));
            else gitAdd(args[1], System.getProperty("user.dir"));
        }
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
        File objDir = new File(prePath + "/.kokit/objects");
        File indexFile = new File(prePath +"/.kokit/index");
        // if a file dotKokit exists, delete it and renitialized
        File dotKokit = new File(prePath + "/" + ".kokit");
        if (!dotKokit.exists() || !dotKokit.isDirectory()) {
            initSuccess = objDir.mkdirs();
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(indexFile))
            ) {
                Index index = new Index();
                output.writeObject(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (initSuccess) {
                System.out.println("Initialized empty Kokit repository in " 
                + prePath + "/.kokit");
                return initSuccess;
            }
        } else {
            deleteFile(dotKokit.getAbsolutePath());
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(indexFile))
            ) {
                initSuccess = objDir.mkdirs();
                Index index = new Index();
                output.writeObject(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (initSuccess) {
                System.out.println("Reinitialized empty Kokit repository in " 
                + prePath + "/.kokit");
                return initSuccess;
            }
        }
        return initSuccess;
    }

    // kokit add
    public static void gitAdd(String fileName, String prePath) {
        File file = new File(prePath + "/" + fileName);
        if (!file.exists()) {
            System.out.println("fatal: pathspec '" + fileName + "' did not match any files");
            return;
        }
        String dotGitpath = getDotGitpath(prePath);
        try {
            updateIndex(fileName, prePath, dotGitpath);
            generateBlob(fileName, prePath, dotGitpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gitAddAll(String prePath) {
        // System.out.println("now user dir is:" + System.getProperty("user.dir"));
        File preDir = new File(prePath);
        File[] files = preDir.listFiles();
        for (File f : files) {
            if (f.isFile()) gitAdd(f.getName(), prePath);
            if (f.isDirectory()) {
                if(f.getName().equals(".kokit")) continue;
                // System.out.println(prePath + "/" + f.getName());
                String subPath = prePath + "/" + f.getName();
                gitAddAll(subPath);
            }
        }
    }


    // #TODO : git commit
    // kokit commit (-m "notes")
    public static void gitCommit(String option1, String option2) {
        if (!option1.equals("-m")) {
            System.out.println(option1 + " is not supported by koki-git.");
        }
    }

    // kokit --help
    public static void gitHelp() {
        System.out.println("HELP: ");
        System.out.println("kokit init : init a local repository in present path.");
        System.out.println("kokit add <filename> : add file to staging area.");
        System.out.println("kokit add . : add all the files under the path to staging area");
    }

    // generate blob object and write in
    public static void generateBlob(String fileName, String srcPath, String dotKokitPath) throws Exception{
        System.out.println("DEBUG: generating blob of " + fileName);
        byte[] content = readFileByBytes(srcPath + "/" + fileName);
        // get SHA-1 value
        String hashtext = getHashOfByteArray(content);
        String objPath = new String(dotKokitPath + "/objects/");
        File blobDir = new File(objPath + hashtext.substring(0, 2));
        blobDir.mkdir();
        System.out.println(blobDir);
        File desfile = new File(blobDir + "/" + hashtext.substring(2, 40));
        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(desfile))
        ) {
            Blob blob = 
            new Blob(content, getOccupyOfByteArray(content), hashtext);
            output.writeObject(blob);
            System.out.println("DEBUG: Object blob has been written down in objects directory ->" + fileName);
        }
    }

    // get SHA-1 hash value of byte[]
    public static String getHashOfByteArray(byte[] content) throws Exception{
        MessageDigest complete = MessageDigest.getInstance("SHA-1");
        complete.update(content);
        byte[] sha1 = complete.digest();
        String hashValue = "";
        for(int j = 0; j < sha1.length; j++) {
            hashValue += Integer.toString((sha1[j] >> 4) & 0x0F, 16) 
                      + Integer.toString(sha1[j] & 0x0F, 16);
        }
        return hashValue;
    }

    // get SHA-1 hash value of file
    public static String getHashOfFile(String filePath) throws Exception {
        byte[] content = readFileByBytes(filePath);
        String hashtext = getHashOfByteArray(content);
        return hashtext;
    }

    // check whether a file is under present path, recursively
    public static Boolean findFile(String fileName, String path) {
        boolean find = false;
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                if (f.getName().equals(fileName)) find = true;
            }
            if (f.isDirectory()) {
                find = findFile(fileName, f.getName() + "/");
            }
        }
        return find;
    }

    // check whether a dir exists in present path, recursively
    public static Boolean findDir(String dirName, String path) {
        boolean find = false;
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                if (f.getName().equals(dirName)) {
                    find = true;
                    break;
                } else {
                    find = findDir(dirName, f.getName() + "/");
                }
            }
        }
        return find;
    }

    // read file content and return to string
    public static String readFileByString(String filename) throws IOException{
        FileInputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[10];
        StringBuilder sb = new StringBuilder();
        while (fis.read(buffer) != -1) {
            sb.append(new String(buffer));
            buffer = new byte[10];
        }
        fis.close();
        String content = sb.toString();
        return content;
    }

    // read file content and return to byte[]
    public static byte[] readFileByBytes(String fileName) throws IOException {
		try (
            InputStream in = 
            new BufferedInputStream(new FileInputStream(fileName)); 
			ByteArrayOutputStream out = 
            new ByteArrayOutputStream();
        ) {
			byte[] tempbytes = new byte[in.available()];
			for (int i = 0; (i = in.read(tempbytes)) != -1;) {
				out.write(tempbytes, 0, i);
			}
			return out.toByteArray();
		}
	}

    // get the actual length of byte[]
    public static int getOccupyOfByteArray(byte[] data) {
        int len = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '\0') break;
            else len ++;
        }
        return len;
    }

    // get dotGitPath #TODO
    public static String getDotGitpath(String prePath) {
        String res = new String();
        File file = new File(prePath);
        if (findDir(".kokit", prePath)) {
            return (prePath + "/.kokit");
        } else {
            res = getDotGitpath(file.getParent());
        }
        if (res.isEmpty()) {
            System.out.println("fatal: not a git repository (or any of the parent directories): .kokit");
        }
        return res;
    }

    // delet all the files under the path, path should be absolute path
    public static void deleteFile(String prePath) {
        File preFile = new File(prePath);
        File[] files = preFile.listFiles();
        for (File f : files) {
            if (f.isFile()) f.delete();
            if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
                f.delete();
            }
        }
    }

    // update index file when git add
    public static void updateIndex(String fileName, String prePath, String dotGitPath) {
        try (
            FileInputStream file = new FileInputStream(dotGitPath + "/index");
        ) {
            Index index = (Index) new ObjectInputStream(file).readObject();
            String hashText = getHashOfFile(prePath + "/" +fileName);
            index.set(fileName, hashText);
            // System.out.println(index.getValue(fileName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
