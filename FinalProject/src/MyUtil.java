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
import java.util.Scanner;

public class MyUtil {

    /*--------------------init operations----------------- */

    // check whether present path is under .corgit dir
    public static boolean checkPathInDotCorgit() {
        boolean pathIncorgit = false;
        // can't use corgit in .corgit directory
        String prePath = System.getProperty("user.dir");
        if (prePath.contains(".corgit")) {
            System.out.println("corgit can't be used recursively. " + 
            "Please check you workspace.");
            pathIncorgit = true;
        }
        return pathIncorgit;
    }

    // ask for whether to reinitialize the working space
    public static boolean askForReinitialize() {
        System.out.println("corgit had been initialized before, are you sure to " + 
            "renitialize? all data before will be removed. y/n");
        Scanner sc = new Scanner(System.in);
        char yn = sc.next().charAt(0);
        sc.close();
        if (yn == ('y') || yn == ('Y')) return true;
        else if (yn == ('n') || yn == ('N')) return false;
        else return false;
    }

    // do some initializing work in dir of .corgit
    public static boolean initCorgit() {
        boolean initSuccess = false;
        // prepare all the dirs and files for initializing
        String prePath = System.getProperty("user.dir");
        String sep = File.separator;
        File dotcorgit = new File(prePath + sep + ".corgit");
        File objDir = new File(dotcorgit.getAbsolutePath()+ sep + "objects");
        File logsDir = new File(dotcorgit.getAbsolutePath() + sep + "logs");
        File headFile = new File(logsDir.getAbsolutePath() + sep + "HEAD");
        File indexFile = new File(dotcorgit.getAbsolutePath() + sep + "index");

        // check : if there is no file or dir called ".corgit", then init
        // else if a dir named dotcorgit exists, choose whether to reinit again
        // if choose 'y', delete it and renitialized, otherwise return false
        if (!dotcorgit.exists() || !dotcorgit.isDirectory()) {
            // create dirs first, so that the FileOutPutStream can find path
            initSuccess = objDir.mkdirs() && logsDir.mkdirs();
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(indexFile))
            ) {
                Index index = new Index();
                output.writeObject(index);
                // init success only when all files and dirs are made
                initSuccess = headFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Fualts were made when index file is creating.");
            }
            // if init success, print out notes
            if (initSuccess) {
                System.out.println("Initialized empty corgit repository in " 
                + prePath + "/.corgit");
                MyUtil.printLogo();
                return initSuccess;
            }
        } else {
            // the workspace has been initialized before, ask for whether to reinit
            if (!askForReinitialize()) return initSuccess;
            // reinit, clear the repoistory and reinit
            deleteFile(dotcorgit.getAbsolutePath());
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(indexFile))
            ) {
                initSuccess = objDir.mkdirs() && logsDir.mkdirs();
                Index index = new Index();
                // write empty index file 
                output.writeObject(index);
                headFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (initSuccess) {
                System.out.println("Reinitialized empty corgit repository in " 
                + prePath + "/.corgit");
                MyUtil.printLogo();
                return initSuccess;
            }
        }
        return initSuccess;
    }


    /*-------------------------corgit add----------------------------- */
    //TODO
    public static boolean checkFileInIndex(String filePath) {
        boolean inIndex = false;
        return inIndex;
    }
    //TODO
    public static void removeFileInIndex(String filePath) {

    }

    // use to show index file content
    public static void showIndex(String prePath) throws Exception{
        String sep = File.separator;
        String indexPath = getDotCorgitPath(prePath) + sep + "index";
        FileInputStream fis = new FileInputStream(indexPath);
        Index index = (Index) new ObjectInputStream(fis).readObject();
        index.listItems();
        fis.close();
    }


    /*-----------------------file\dir operating----------------------- */

    // delet all the files under a path, path should be absolute path
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

    // check whether a file is under present path, recursively
    public static Boolean findFile(String fileName, String path) {
        String sep = File.separator;
        boolean find = false;
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                if (f.getName().equals(fileName)) find = true;
            }
            if (f.isDirectory()) {
                find = findFile(fileName, f.getName() + sep);
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

    // check whether a dir exists in present path, recursively
    public static Boolean findDir(String dirName, String path) {
        boolean find = false;
        File dir = new File(path);
        // if dir is not directory, return false
        if (dir.isFile()) return find;
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                if (f.getName().equals(dirName)) {
                    find = true;
                    break;
                } else {
                    find = findDir(dirName, f.getAbsolutePath());
                }
            }
        }
        return find;
    }

    /*------------------generate hashvalue SHA-1------------------- */
    
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
        byte[] content = MyUtil.readFileByBytes(filePath);
        String hashtext = getHashOfByteArray(content);
        return hashtext;
    }


    /*------------------path searching operations-------------------*/

    // get dotCrogitPath 
    public static String getDotCorgitPath(String prePath) {
        // System.out.println("prePath in getDotCorgitPath: " + prePath);
        String res = new String();
        File file = new File(prePath);
        if (findDir(".corgit", prePath)) {
            return (prePath + "/.corgit");
        } else {
            res = getDotCorgitPath(file.getParent());
        }
        if (res.isEmpty()) {
            System.out.println("fatal: not a git repository (or any of the parent directories): .corgit");
        }
        return res;
    }

    // get objects path
    public static String getObjPath(String prePath) {
        // System.out.println("prePath in getObjPath: " + prePath);
        return getDotCorgitPath(prePath) + "/objects";
    }

    /*-----------------beautification operations----------------------*/

    // print out corgit logo 
    public static void printLogo() {
        System.out.println('\n' +
            "██████╗ ██████╗ ██████╗  ██████╗ ██╗████████╗ " + '\n' +
            "██╔════╝██╔═══██╗██╔══██╗██╔════╝ ██║╚══██╔══╝" + '\n' +
            "██║     ██║   ██║██████╔╝██║  ███╗██║   ██║   " + '\n' +
            "██║     ██║   ██║██╔══██╗██║   ██║██║   ██║   " + '\n' +
            "╚██████╗╚██████╔╝██║  ██║╚██████╔╝██║   ██║   " + '\n' +
            " ╚═════╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝   ╚═╝   " + '\n' 
        );
    }

    /*-------------------------corgit help--------------------------- */
    public static void printHelpDoc() {
        System.out.println("HELP: ");
        System.out.println("corgit init : init a local repository in present path.");
        System.out.println("corgit add <filename> : add file to staging area.");
        System.out.println("corgit add . : add all the files under the path to staging area");
    }

    /*---------------------------get length-------------------------- */

    // get the actual length of byte[]
    public static int getOccupyOfByteArray(byte[] data) {
        int len = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '\0') break;
            else len ++;
        }
        return len;
    }
}
