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
import java.util.ArrayList;
import java.util.Scanner;


public class MyUtil {

    /*--------------------init operations----------------- */
    // check whether the present path has been initialized, every time we
    // use a corgit instructor, we need to check it
    public static boolean checkInitialized(String curPath) {
        return true;
    }

    // check whether present path is under .corgit dir
    public static boolean checkPathInDotCorgit() {
        boolean pathIncorgit = false;
        // can't use corgit in .corgit directory
        String curPath = System.getProperty("user.dir");
        if (curPath.contains(".corgit")) {
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
        String curPath = System.getProperty("user.dir");
        String sep = File.separator;
        File dotcorgit = new File(curPath + sep + ".corgit");
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
                + curPath + "/.corgit");
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
                + curPath + "/.corgit");
                MyUtil.printLogo();
                return initSuccess;
            }
        }
        return initSuccess;
    }


    /*-------------------------corgit add----------------------------- */

    public static boolean checkFileInIndex(String fileName, String curPath) {
        boolean inIndex = false;
        String sep = File.separator;
        String indexPath = getDotCorgitPath(curPath) + sep + "index";
        String fileRelativePath = getFileRelativePath(fileName, curPath);
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
    public static boolean removeFileInIndex(String fileName, String curPath) {
        boolean remove = false;
        String sep = File.separator;
        String indexPath = getDotCorgitPath(curPath) + sep + "index";
        String fileRelativePath = getFileRelativePath(fileName, curPath);
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
    public static void showIndex(String curPath) throws Exception{
        String sep = File.separator;
        String indexPath = getDotCorgitPath(curPath) + sep + "index";
        // this function dose not modify index object, so there is no need to write back
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexPath));
        Index index = (Index) ois.readObject();
        index.listItems();
        ois.close();
    }

    // use to show head file content
    public static void showHead(String curPath) throws Exception{
        String sep = File.separator;
        String headPath = getDotCorgitPath(curPath) + sep + "HEAD";
        // this function dose not modify index object, so there is no need to write back
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(headPath));
        Head head = (Head) ois.readObject();
        System.out.println(head.getCommitId());
        ois.close();
    }

    // while we use add all, or we add a dir contains file that was deleted,
    // we should check whether some items need to de removed in index file
    public static boolean removeFileDeletedInIndex(String curPath) {
        boolean removeSuccess = false;
        String sep = File.separator;
        String indexPath = getIndexFilePath(curPath);
        // read object and modify it
        try {
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            String[] indexItemKeys = index.getKeys();
            for (String key : indexItemKeys) {
                File file = new File(curPath + sep + key);
                String relPathOfcurPath = getFileRelativePath("", curPath);
                if (key.contains(relPathOfcurPath) && !file.exists()) {
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
    public static boolean addToIndex(String fileName, String curPath) {
        System.out.println("Debug: generating index file of" + fileName);

        boolean addSuccess = false;
        String sep = File.separator;
        String filePath = curPath + sep + fileName;
        String indexPath = getIndexFilePath(curPath);

        // index store relative path, so get it
        String fileRelativePath = getFileRelativePath(fileName, curPath);

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

    // delete index item
    public static boolean deleteToIndex(String fileName, String curPath) {
        boolean deleteSuccess = false;
        String relativePath = MyUtil.getFileRelativePath(fileName, curPath);
        String indexPath = MyUtil.getIndexFilePath(curPath);
        try {
            // read object and modify it
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            index.removeItem(relativePath);
            ois.close();

            // write object after modified, important!!!
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();

            //#BUG
            System.out.println("index file has been deleted. \n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deleteSuccess;
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

    public static Boolean generateBlob(String fileName, String curPath) {
        boolean genSuccess = false;
        String sep = File.separator;
        String filePath = curPath + sep + fileName;
        String dotCorgitPath = MyUtil.getDotCorgitPath(curPath);
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
            Blob blob = new Blob(content);
            output.writeObject(blob);

            //#BUG
            System.out.println("DEBUG: Object blob has been written down in objects directory ->" + fileName +'\n');

            genSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genSuccess;
    }

    // generate tree object when commit
    public static String generateTree() throws Exception{

            // read out index file to get the files information
            String indexPath = MyUtil.getIndexFilePath(System.getProperty("user.dir"));
            FileInputStream file = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(file);
            Index index = (Index) ois.readObject();
    
            String sep = File.separator;
            String[] indexItems = index.getKeys();
            ArrayList<String[]> als = new ArrayList<>();
    
            for (String item : indexItems) {
                als.add(item.split(sep));
            }
            int maxLayNumber = 0;
            for (String[] strArr : als) {
                maxLayNumber = Math.max(maxLayNumber, strArr.length);
            }
            
            int countNumber = maxLayNumber -1;
            System.out.println(maxLayNumber);
            String commitId = "";
            while (countNumber >= 0) {
                Tree tree = new Tree();
                for (String[] strArr : als) {
                    if (countNumber == strArr.length) {
                        tree.addBlob(strArr[strArr.length - 1], index.getValue(strArr[strArr.length - 1]));
                        if (countNumber == 0) {
                            tree.setTreeName("root");
                        } else {
                            tree.setTreeName(strArr[countNumber - 1]);
                        }
                    } else if (strArr.length > countNumber){
                        tree.addTree(strArr[countNumber], index.getValue(strArr[countNumber]));
                    } else continue;
                }
                
                String treeHash = tree.getTreeHash();
                if (countNumber == 0) {
                    commitId = treeHash;
                }
                String objPath = MyUtil.getObjPath(System.getProperty("user.dir"));
                File treeDir = new File(objPath + sep + treeHash.substring(0, 2));
                treeDir.mkdir();
                File treeFile = new File(treeDir + sep + treeHash.substring(2, 40));
                FileOutputStream fos = new FileOutputStream(treeFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tree);
                oos.close();
                countNumber --;
            }
            ois.close();
            System.out.println("DEBUG: Object tree has been written down in objects directory\n");
            return commitId;
        }
    
    // generate commit object when commit
    public static void generateCommit(String lastCommitId, String commitId, String message, String time) throws Exception {
            Commit commit = new Commit(lastCommitId, commitId, message, time);
            String sep = File.separator;
            String content = commit.toString();
            String curPath = System.getProperty("user.dir");
            String objPath = MyUtil.getObjPath(curPath);
            String hashText = MyUtil.getHashOfByteArray(content.getBytes());
            File commitDir = new File(objPath + sep + hashText.substring(0, 2));
            commitDir.mkdir();
            File desfile = new File(commitDir + sep + hashText.substring(2, 40));
    
            try (
                ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(desfile))
            ) {
                output.writeObject(commit);
                System.out.println("DEBUG: Commit has been written down in objects directory \n");
            } 
        }


    /*-----------------------file\dir operating----------------------- */

    // delet all the files under a path, path should be absolute path
    public static void deleteFile(String curPath) {
        File preFile = new File(curPath);
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
    public static String getDotCorgitPath(String curPath) {
        // System.out.println("curPath in getDotCorgitPath: " + curPath);#BUG
        String sep = File.separator;
        String res = new String();
        File file = new File(curPath);
        if (findDir(".corgit", curPath)) {
            return (curPath + sep + ".corgit");
        } else {
            res = getDotCorgitPath(file.getParent());
        }
        if (res.isEmpty()) {
            System.out.println("fatal: not a git repository (or any of the parent directories): .corgit");
        }
        return res;
    }

    // get objects path
    public static String getObjPath(String curPath) {
        // System.out.println("curPath in getObjPath: " + curPath);
        return getDotCorgitPath(curPath) + "/objects";
    }

    // get file's relative path to corgit repository
    public static String getFileRelativePath(String fileName, String curPath) {
        String sep = File.separator;
        String filePath = curPath + sep + fileName;
        // because curPath is always exists, so getDotCorgit uses curPath
        // System.out.println("pre path in getFileRP : " + curPath);#BUG
        String dotCorgitPath = MyUtil.getDotCorgitPath(curPath);
        File dotCorgit = new File(dotCorgitPath);
        String rootPath = dotCorgit.getParent();
        // get relative path
        String fileRelativePath = filePath.replace(rootPath + sep, "");
        return fileRelativePath;
    }

    // get the root path of a corgit repository
    public static String getRootPathOfCorigitRepo(String curPath) {
        String dotCorgitPath = MyUtil.getDotCorgitPath(curPath);
        File dotCorgit = new File(dotCorgitPath);
        String rootPath = dotCorgit.getParent(); 
        return rootPath;
    }

    // get the path where index file exists
    public static String getIndexFilePath(String curPath) {
        String sep = File.separator;
        String dotCorgitPath = MyUtil.getDotCorgitPath(curPath);
        String indexPath = dotCorgitPath + sep + "index";
        return indexPath;
    }

    // get the path where HEAD file exsits
    public static String getHeadFilePath(String curPath) {
        String sep = File.separator;
        String dotCorgitPath = MyUtil.getDotCorgitPath(curPath);
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
        System.out.println("corgit add <filename> : add file to staged area.");
        System.out.println("corgit add . : add all the files under the path to staging area");
        System.out.println("corgit add <dirname> : add dir to staging area.");
        System.out.println("corgit commit -m <comment> : add the staged information to committed area.");
        System.out.println("corgit ls-files --stage : print the content of index.");
        System.out.println("corgit ls-head : show the commitID in HEAD file.");
        System.out.println("corgit rm <filename> : delete file in both working space and staged area.");
        System.out.println("corgit rm <dirname> : delete directory in both working space and staged area.");
        System.out.println("corgit rm . : delete all the files/dirs under current working space and staged area.");
        System.out.println("corgit rm --cached <filename> : delete the file in staged area.");
        System.out.println("corgit rm --cached <dirname> : delete all the files under the dir in staged area.");
        System.out.println("corgit rm --cached . : delete all the conten of staged area.");
    }

    // print args[]
    public static String printArgs(String[] args) {
        String res = "";
        for (String arg : args) {
            res += arg;
            res += " ";
        }
        return res;
    }

    // note for wrong format instructions
    public static void wrongFormat(String args[]) {
        System.out.println("corgit: " + MyUtil.printArgs(args) + "is not a git command. See 'corgit --help' ");
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
