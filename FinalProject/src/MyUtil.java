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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

public class MyUtil {

    /*--------------------init operations------------------- */

    // check whether the present path has been initialized, every time we
    // use a corgit instructor, we need to check it
    public static boolean notInitialized(String curPath) {
        boolean notInitialized = false;
        String sep = File.separator;
        String dotCorgitPath = getDotCorgitPath(curPath);
        File file = new File(curPath + sep + ".corgit");
        if (dotCorgitPath.isEmpty() || 
            (dotCorgitPath.contains(curPath) && !file.exists())) {
                notInitialized = true;
        }
        if (notInitialized == true) {
            System.out.println("fatal: not a corgit repository (or any of the parent directories): .git");
        }
        return notInitialized;
    }

    // check whether present path is under .corgit dir
    public static boolean checkPathInDotCorgit(String curPath) {
        boolean pathIncorgit = false;
        // can't use corgit in .corgit directory
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
        // System.out.println("Checking whether the file '" + fileRelativePath + "' was in staged area..."); //#BUG
        // deserialization operation
        try (FileInputStream fis = new FileInputStream(indexPath)) {
            Index index = (Index) new ObjectInputStream(fis).readObject();
            inIndex = index.findItem(fileRelativePath);
            // if index item contians a fileName but no item equals the name
            // that means the filename is actually a dirName, and there are 
            // files in this dir are stored in index file 
            // inIndex = index.containFileName(fileRelativePath);#BUG
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return inIndex;
    }

    // delete file in index
    public static boolean deleteToIndex(String fileName, String curPath) {
        boolean remove = false;
        String sep = File.separator;
        String indexPath = getDotCorgitPath(curPath) + sep + "index";
        String fileRelativePath = getFileRelativePath(fileName, curPath);
        // System.out.println("fRP is : " + fileRelativePath);
        try {
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            remove = index.removeItem(fileRelativePath);

            ois.close();
            // write object after modified, important!!!
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return remove;
    }

    // remove those items in index file that contian a dirname
    public static boolean removeNameContainsInIndex(String fileName, String curPath) {
        boolean removeSuccess = false;
        String indexPath = getIndexFilePath(curPath);
        String relativeFilePath = getFileRelativePath(fileName, curPath);
        // read object and modify it
        try {
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            String[] indexItemKeys = index.getKeys();
            for (String key : indexItemKeys) {
                if (key.contains(relativeFilePath)) {
                    System.out.println("rm : " + key);
                    index.removeItem(key);
                }
            }
            removeSuccess = true;
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

    // use to get head file content, and choose whether to print out the content
    public static String showHead(String curPath, boolean printOut) {
        String res = new String();
        String sep = File.separator;
        String headPath = getDotCorgitPath(curPath) + sep + "HEAD";
        // this function dose not modify index object, so there is no need to write back
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(headPath));
            Head head = (Head) ois.readObject();
            res = head.getCommitId();
            ois.close();
            if (!printOut) return res;
            if (head.getCommitId().isEmpty()) {
                System.out.println("Empty HEAD, no commit history");
            } else {
                System.out.println(head.getCommitId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
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
            System.out.println("add : " + fileRelativePath);

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

    public static Boolean generateBlob(String fileName, String curPath) {
        boolean genSuccess = false;
        String sep = File.separator;
        String filePath = curPath + sep + fileName;
        String dotCorgitPath = MyUtil.getDotCorgitPath(curPath);
        String objPath = dotCorgitPath + sep + "objects";

        // read file to bytes
        byte[] content = MyUtil.readFileByBytes(filePath);
        // get SHA-1 value
        String hashtext = MyUtil.getHashOfByteArray(content);
        File blobDir = new File(objPath + sep + hashtext.substring(0, 2));
        blobDir.mkdir();
        File blobFile = new File(blobDir + sep + hashtext.substring(2, 40));

        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(blobFile))
        ) {
            Blob blob = new Blob(content);
            output.writeObject(blob);

            //#BUG
            System.out.println("DEBUG: Object blob has been written down in objects directory ->" + fileName);

            genSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genSuccess;
    }

    /*------------------------corgit commit------------------------*/

    // generate tree object when commit
    public static String generateTree() throws Exception{
        // read out index file to get the files information
        String indexPath = MyUtil.getIndexFilePath(System.getProperty("user.dir"));
        FileInputStream file = new FileInputStream(indexPath);
        ObjectInputStream ois = new ObjectInputStream(file);
        Index index = (Index) ois.readObject();
        // core algorithm for building tree !!!
        String[] indexItems = index.getKeys();
        MTreeNode root = buildMTree(indexItems);
        LinkedList<MTreeNode> postList = root.postOrder();
        //BUG hashvalue 这里是空，因为还没设置
        // for (MTreeNode item : postList) {
        //     System.out.println("item is : " + item.getNodeName() + "; item's index name is " + item.getIndexName(item) + "; item's parent's name is " + item.getParent().getNodeName());
        // }
        for (MTreeNode node: postList) {
            if (node.isLeaf()) {
                // ensure that all blob node has hashValue
                node.setHashValue(index.getValue(node.getIndexName(node)));
            }
            if (!node.isLeaf()) {
                Tree tree = new Tree();
                ArrayList<MTreeNode> children = node.getChildList();
                String blobHashTexts = new String();
                for (MTreeNode child : children) {
                    blobHashTexts += child.getHashValue();
                    tree.addBlob(child.getNodeName(), child.getHashValue());
                }
                String treeHash = getHashOfByteArray(blobHashTexts.getBytes());
                tree.setTreeName(treeHash);
                node.setHashValue(treeHash);

                String objPath = MyUtil.getObjPath(System.getProperty("user.dir"));
                String sep = File.separator;
                File treeDir = new File(objPath + sep + treeHash.substring(0, 2));
                treeDir.mkdir();
                File treeFile = new File(treeDir + sep + treeHash.substring(2, 40));
                FileOutputStream fos = new FileOutputStream(treeFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tree);
                oos.close();
            }
        }
        ois.close();
        return root.getHashValue();
    }

    // build Mtree is so so so important!!!
    public static MTreeNode buildMTree(String[] indexItems) {
        String sep = File.separator;
        MTreeNode root = new MTreeNode("root");
        ArrayList<String[]> als = new ArrayList<>();
        for (String item : indexItems) {
            als.add(item.split(sep));
        }
        for (String[] strArr : als) {
            HashMap<Integer, MTreeNode> MTreeMap = new HashMap<>();
            for (int i = 0; i < strArr.length; i++) {
                if (i == 0) {
                    if (!root.hasChildNamed(strArr[i])) {
                        MTreeNode node = new MTreeNode(strArr[i]);
                        MTreeMap.put(i, node);
                        root.addChild(node);
                    } else if (root.hasChildNamed(strArr[i])) {
                        MTreeMap.put(i, root.getChildNamed(strArr[i]));
                    }
                } else {
                    if (!MTreeMap.get(i - 1).hasChildNamed(strArr[i])) {
                        MTreeNode node = new MTreeNode(strArr[i]);
                        MTreeMap.put(i, node);
                        MTreeMap.get(i -1).addChild(node);
                    } else if (MTreeMap.get(i - 1).hasChildNamed(strArr[i])) {
                        MTreeMap.put(i, MTreeMap.get(i - 1).getChildNamed(strArr[i]));
                    }
                }
            }
        }
        return root;
    }

    
    // generate commit object when commit
    public static void generateCommit(String lastCommitId, String commitId, String treeId, String message, String time) throws Exception {
        Commit commit = new Commit(lastCommitId, commitId, treeId, message, time);
        // if nothing changed, working tree clean
        if (lastCommitId.equals(commitId)) {
            System.out.println("nothing to commit, working tree clean");
            return; 
        }
        String sep = File.separator;
        String curPath = System.getProperty("user.dir");
        String objPath = MyUtil.getObjPath(curPath);
        String hashText = commitId;
        File commitDir = new File(objPath + sep + hashText.substring(0, 2));
        commitDir.mkdir();
        File desfile = new File(commitDir + sep + hashText.substring(2, 40));

        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(desfile))
        ) {
            output.writeObject(commit);
            System.out.println("Commit done.");
            printCommitChange(lastCommitId, commitId);
        } 
    }

    // get the diff between two commits and print the infomation
    public static boolean printCommitChange(String lastCommitId, String commitId) {
        boolean changed = false;
        String lastCommitID = lastCommitId;
        String curCommitID = commitId;
        HashMap<String, String> lastCommitMap = new HashMap<>();
        HashMap<String, String> curCommitMap = new HashMap<>();
        // if last commit is not empty, read all the items commited last time
        if (!lastCommitID.isEmpty()) {
            lastCommitMap = readCommit(lastCommitID);
        }
        curCommitMap = readCommit(curCommitID);
        Set<String> addArr = new HashSet<>();
        Set<String> delArr = new HashSet<>();
        Set<String> modArr = new HashSet<>();
        
        Set<String> set1 = lastCommitMap.keySet();
        String[] lastCommitFileNames = set1.toArray(new String[set1.size()]);
        Set<String> set2 = curCommitMap.keySet();
        String[] curCommitFileNames = set2.toArray(new String[set2.size()]);
        // use new commit items to check old commit items to get what are added
        // and what are modified
        for (String fileName : curCommitFileNames) {
            //BUG
            System.out.println(fileName);
            String hashValue = curCommitMap.get(fileName);
            if (lastCommitMap.containsKey(fileName)) {
                if (!lastCommitMap.get(fileName).equals(hashValue)) {
                    modArr.add(fileName);
                } 
            } else {
                addArr.add(fileName);
            }
        }
        // use old commit items to check new commit items to get what are deleted
        for (String fileName : lastCommitFileNames) {
            String hashValue = lastCommitMap.get(fileName);
            if (curCommitMap.containsKey(fileName)) {
                if (!curCommitMap.get(fileName).equals(hashValue)) {
                    modArr.add(fileName);
                }
            } else {
                delArr.add(fileName);
            }
        }
        int change = addArr.size() + modArr.size() + delArr.size();
        System.out.println(change + " files has been changed.");
        System.out.println(addArr.size() + " files add, " + delArr.size() + " files delete, " + modArr.size()
        + " files modify");
        if (addArr.size() != 0) {
            for (String item : addArr) {
                System.out.println("create mode " + item);
            }
        }
        if (modArr.size() != 0) {
            for (String item : delArr) {
                System.out.println("delete mode " + item);
            }
        }
        if (delArr.size() != 0) {
            for (String item : delArr) {
                System.out.println("modify mode " + item);
            }
        }
        return changed;
    }

    // read all the items of one commit
    public static HashMap<String, String> readCommit(String commitId) {
        String curPath = System.getProperty("user.dir");
        String sep = File.separator;
        HashMap<String, String> commitMap = new HashMap<>();
        // System.out.println("readCommit->commitId: " + commitId);
        String commitPath = getObjPath(curPath) + 
        sep + commitId.substring(0, 2) +
        sep + commitId.substring(2, 40);
        // System.out.println("readCommit->commitPath :" + commitPath);
        String rootTreeId = new String();
        try {
            FileInputStream fis = new FileInputStream(commitPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Commit commit = (Commit) ois.readObject();
            rootTreeId = commit.getTreeId();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        commitMap = readTree(rootTreeId);
        // System.out.println("readCommit-> : rootTreeId : " + rootTreeId);
        return commitMap;
    }

    // read all the item in a tree
    public static HashMap<String, String> readTree(String treeId) {
        HashMap<String, String> treeMap = new HashMap<>();
        String curPath = System.getProperty("user.dir");
        String sep = File.separator;
        String treePath = getObjPath(curPath) + 
        sep + treeId.substring(0, 2) +
        sep +  treeId.substring(2, 40);
        System.out.println("readTree->treePath : " + treePath);
        try {
            FileInputStream fis = new FileInputStream(treePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Tree tree = (Tree) ois.readObject();
            System.out.println(tree.toString());
            treeMap.putAll(tree.getBlobMap());
            if (!tree.getTreeMap().isEmpty()) {
                Set<String> set = tree.getTreeMap().keySet();
                String[] keys = set.toArray(new String[set.size()]);
                for (String key : keys) {
                    System.out.println("sub trees: " + key);
                    System.out.println(tree.getTreeId(key));
                    treeMap.putAll(readTree(tree.getTreeId(key)));
                }
            }
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return treeMap;
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
        // System.out.println(path);
        File[] files = dir.listFiles();
        try {
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
        } catch (Exception e) {
            return find;
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
            res = curPath + sep + ".corgit";
        } else {
            // System.out.println(curPath);//#TODO:when not init, add and error
            // System.out.println(res);
            res = getDotCorgitPath(file.getParent());
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
        /*
        relative path may not exist, but we can always return one because curPath
        always exists, and this func just do string operation, rather than real
        file path operation 
        */
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

    /*----------------------count numbers---------------------------- */
    public static int getNameAppearedTimesInIndex(String fileName, String curPath) {
        int count = 0;
        String indexPath = getIndexFilePath(curPath);
        String fileRelativeName = getFileRelativePath(fileName, curPath);
        try {
            // read object and modify it
            FileInputStream fis = new FileInputStream(indexPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Index index = (Index) ois.readObject();
            count = index.getAppearedTimes(fileRelativeName);
            ois.close();
            // write object after modified, important!!!
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
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
