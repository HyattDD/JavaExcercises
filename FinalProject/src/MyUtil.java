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
    // check whether the present path has been initialized, every time we
    // use a corgit instructor, we need to check it
    public static boolean checkInitialized(String prepath) {
        return true;
    }

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
        // File logsDir = new File(dotcorgit.getAbsolutePath() + sep + "logs");
        File headFile = new File(dotcorgit.getAbsolutePath() + sep + "HEAD");
        File indexFile = new File(dotcorgit.getAbsolutePath() + sep + "index");

        // check : if there is no file or dir called ".corgit", then init
        // else if a dir named dotcorgit exists, choose whether to reinit again
        // if choose 'y', delete it and renitialized, otherwise return false
        if (!dotcorgit.exists() || !dotcorgit.isDirectory()) {
            // create dirs first, so that the FileOutPutStream can find path
            initSuccess = objDir.mkdirs();
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(indexFile))
            ) {
                Index index = new Index();
                output.writeObject(index);
                // init success only when all files and dirs are made
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Fualts were made when index file is creating.");
            }
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(headFile))
            ) {
                Head head = new Head("");
                output.writeObject(head);
                initSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Faults were made when head file is creating.");
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
                initSuccess = objDir.mkdirs();
                Index index = new Index();
                // write empty index file 
                output.writeObject(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(headFile))
            ) {
                Head head = new Head("");
                output.writeObject(head);
                initSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Faults were made when head file is creating.");
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

    public static boolean checkFileInIndex(String fileName, String prePath) {
        boolean inIndex = false;
        String sep = File.separator;
        String indexPath = getDotCorgitPath(prePath) + sep + "index";
        String fileRelativePath = getFileRelativePath(fileName, prePath);
        System.out.println("fileRP in checkFileInIndex is : " + fileRelativePath);
        // deserialization operation
        try (FileInputStream fis = new FileInputStream(indexPath)) {
            Index index = (Index) new ObjectInputStream(fis).readObject();
            inIndex = index.findItem(fileRelativePath);
            // if index item contians a fileName but no item equals the name
            // that means the filename is actually a dirName, and there are 
            // files in this dir are stored in index file 
            inIndex = index.containFileName(fileRelativePath);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return inIndex;
    }
    public static boolean removeFileInIndex(String fileName, String prePath) {
        boolean remove = false;
        String sep = File.separator;
        String indexPath = getDotCorgitPath(prePath) + sep + "index";
        String fileRelativePath = getFileRelativePath(fileName, prePath);
        System.out.println("fRP is : " + fileRelativePath);
        try {
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            index.removeItem(fileRelativePath);

            ois.close();
            // write object after modified, important!!!
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();

            remove = true;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return remove;
    }

    // use to show index file content
    public static void showIndex(String prePath) throws Exception{
        String sep = File.separator;
        String indexPath = getDotCorgitPath(prePath) + sep + "index";
        // this function dose not modify index object, so there is no need to write back
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexPath));
        Index index = (Index) ois.readObject();
        index.listItems();
        ois.close();
    }

    // while we use add all, or we add a dir contains file that was deleted,
    // we should check whether some items need to de removed in index file
    public static boolean removeFileDeletedInIndex(String prePath) {
        boolean removeSuccess = false;
        String sep = File.separator;
        String indexPath = getIndexFilePath(prePath);
        // read object and modify it
        try {
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            String[] indexItemKeys = index.getKeys();
            for (String key : indexItemKeys) {
                File file = new File(prePath + sep + key);
                String relPathOfPrePath = getFileRelativePath("", prePath);
                if (key.contains(relPathOfPrePath) && !file.exists()) {
                    //#TODO
                    index.removeItem(key);
                }
            }
            ois.close();
            // write object after modified, important!!!
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removeSuccess;
    }

    // update index file when git add
    public static boolean addToIndex(String fileName, String prePath) {
        System.out.println("Debug: generating index file of" + fileName);

        boolean addSuccess = false;
        String sep = File.separator;
        String filePath = prePath + sep + fileName;
        String indexPath = getIndexFilePath(prePath);

        // index store relative path, so get it
        String fileRelativePath = getFileRelativePath(fileName, prePath);

        try {
            // read object and modify it
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            String hashText = MyUtil.getHashOfFile(filePath);
            index.setItem(fileRelativePath, hashText);
            ois.close();

            // write object after modified, important!!!
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();

            //#BUG
            System.out.println("index file has been written. \n");

            addSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addSuccess;
    }

    // update head file, since head file just maintains one id, 
    // there is no need to deserialize it and rewrite back, just overwrite it
    public static boolean updateHead(String commitId) {
        String headPath = getHeadFilePath(System.getProperty("user.dir"));
        boolean updateHead = false;
        Head head = new Head(commitId);
        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(headPath));
        ) {
            output.writeObject(head);
            updateHead = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateHead;
    }

    public static Boolean generateBlob(String fileName, String prePath) {
        boolean genSuccess = false;
        String sep = File.separator;
        String filePath = prePath + sep + fileName;
        String dotCorgitPath = MyUtil.getDotCorgitPath(prePath);
        String objPath = dotCorgitPath + sep + "objects";

        //#BUG
        System.out.println("DEBUG: generating blob of " + fileName);

        // read file to bytes
        byte[] content = MyUtil.readFileByBytes(filePath);
        // get SHA-1 value
        String hashtext = MyUtil.getHashOfByteArray(content);
        File blobDir = new File(objPath + sep + hashtext.substring(0, 2));
        blobDir.mkdir();
        System.out.println(blobDir);//#BUG
        File blobFile = new File(blobDir + sep + hashtext.substring(2, 40));

        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(blobFile))
        ) {
            Blob blob = 
            new Blob(content);
            output.writeObject(blob);

            //#BUG
            System.out.println("DEBUG: Object blob has been written down in objects directory ->" + fileName +'\n');

            genSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genSuccess;
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
    public static byte[] readFileByBytes(String fileName) {
        byte[] resBytes = null;
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
            resBytes = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resBytes;
    }

    // check whether a dir exists in present path, recursively
    public static Boolean findDir(String dirName, String path) {
        // System.out.println("find dir in :" + path); #BUG
        boolean find = false;
        File dir = new File(path);
        // if dir is not directory, return false
        if (dir.isFile()) return find;
        if (!dir.exists()) return find;
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
    public static String getHashOfByteArray(byte[] content) {
        String hashValue = "";
        try {
            MessageDigest complete = MessageDigest.getInstance("SHA-1");
            complete.update(content);
            byte[] sha1 = complete.digest();
            for(int j = 0; j < sha1.length; j++) {
                hashValue += Integer.toString((sha1[j] >> 4) & 0x0F, 16) 
                          + Integer.toString(sha1[j] & 0x0F, 16);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    // get dotCorgitPath 
    public static String getDotCorgitPath(String prePath) {
        // System.out.println("prePath in getDotCorgitPath: " + prePath);#BUG
        String sep = File.separator;
        String res = new String();
        File file = new File(prePath);
        if (findDir(".corgit", prePath)) {
            return (prePath + sep + ".corgit");
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

    // get file's relative path to corgit repository
    public static String getFileRelativePath(String fileName, String prePath) {
        String sep = File.separator;
        String filePath = prePath + sep + fileName;
        // because prepath is always exists, so getDotCorgit uses prepath
        // System.out.println("pre path in getFileRP : " + prePath);#BUG
        String dotCorgitPath = MyUtil.getDotCorgitPath(prePath);
        File dotCorgit = new File(dotCorgitPath);
        String rootPath = dotCorgit.getParent();
        // get relative path
        String fileRelativePath = filePath.replace(rootPath + sep, "");
        return fileRelativePath;
    }

    // get the root path of a corgit repository
    public static String getRootPathOfCorigitRepo(String prePath) {
        String dotCorgitPath = MyUtil.getDotCorgitPath(prePath);
        File dotCorgit = new File(dotCorgitPath);
        String rootPath = dotCorgit.getParent(); 
        return rootPath;
    }

    // get the path where index file exists
    public static String getIndexFilePath(String prePath) {
        String sep = File.separator;
        String dotCorgitPath = MyUtil.getDotCorgitPath(prePath);
        String indexPath = dotCorgitPath + sep + "index";
        return indexPath;
    }

    // get the path where HEAD file exsits
    public static String getHeadFilePath(String prePath) {
        String sep = File.separator;
        String dotCorgitPath = MyUtil.getDotCorgitPath(prePath);
        String headPath = dotCorgitPath + sep + "HEAD";
        return headPath;
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
