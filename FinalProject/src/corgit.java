import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class corgit {
    public static void main(String[] args) {
        try {
            String prePath = System.getProperty("user.dir");
            // jump to different options
            if (args[0].equals("init")) {
                corgitInit();
            } else if (args[0].equals("add")) {
                if (args[1].equals(".")) {
                    corgitAddAll(prePath);
                } else corgitAdd(args[1], prePath);
            } else if (args[0].equals("commit")) {
                corgitCommit(args[1], args[2], prePath);
            } else if (args[0].equals("--help")) {
                gitHelp();
            } else if (args[0].equals("ls-files") && 
                       args[1].equals("--stage")) {
                corgitListIndex(prePath);
            }
            // corgit help
            else {
                System.out.println("git: " + args[0] + "is not a git command. See 'corgit --help' ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // corgit init
    public static boolean corgitInit() {
        boolean initSuccess = false;
        /* check whether in .corgit path 
        - if in , fail to init and return
        - if not , check whether present path had been initialized before
            - if yes, reinitialized and make dir .corgit clean
            - if not, initialized
        */ 
        if (MyUtil.checkPathInDotCorgit()) {
            return initSuccess;
        } else return MyUtil.initCorgit();
    }


    // corgit add
    public static boolean corgitAdd(String fileName, String prePath) {
        // check initialized
        if (!MyUtil.checkInitialized(prePath)) {
            System.out.println("fatal: not a corgit repository (or any of the parent directories): .git");
            return false;
        }
        boolean addSuccess = false;
        String sep = File.separator;
        String filePath = prePath + sep +fileName;
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                addSuccess = MyUtil.addToIndex(fileName, prePath);
                addSuccess = MyUtil.generateBlob(fileName, prePath);
            } else if (file.isDirectory()) {
                corgitAddAll(file.getAbsolutePath());
            }
        } else if (!file.exists()) {//#TODO
            // notice: since file does not exsit, filePath may not be a real path
            if (MyUtil.checkFileInIndex(fileName, prePath)) {
                addSuccess = MyUtil.removeFileInIndex(fileName, prePath);
                addSuccess = MyUtil.removeFileDeletedInIndex(prePath + sep + fileName);
            } else {
                System.out.println("fatal: pathspec '" + 
                fileName + "' did not match any files");
            }
        }
        return addSuccess;
    }

    public static boolean corgitAddAll(String prePath) {
        boolean addSuccess = false;
        String sep = File.separator;
        File preDir = new File(prePath);
        File[] files = preDir.listFiles();
        //#TODO
        // if some files were deleted in this dir, remove their items in index file
        addSuccess = MyUtil.removeFileDeletedInIndex(prePath);
        for (File f : files) {
            if (f.isFile()) corgitAdd(f.getName(), prePath);
            if (f.isDirectory()) {
                if(f.getName().equals(".corgit")) continue;
                // System.out.println(prePath + "/" + f.getName());
                String subPath = prePath + sep + f.getName();
                corgitAddAll(subPath);
            }
        }
        addSuccess = true;
        return addSuccess;
    }

    // corgit commit (-m "notes")
    public static boolean corgitCommit(String opString01, String opString02, String prePath) throws Exception {
        // check initialized
        if (!MyUtil.checkInitialized(prePath)) {
            System.out.println("fatal: not a corgit repository (or any of the parent directories): .git");
            return false;
        }
        // check input format
        if (!opString01.equals("-m")) {
            System.out.println("use 'corgit commit -m ···' and try again.");
        }
        // do commit, read Head file and change it 
        boolean commitSuccess = false;
        String headPath = MyUtil.getHeadFilePath(prePath);
        FileInputStream fis = new FileInputStream(headPath);
        // System.out.println(headPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Head head = (Head) ois.readObject();

        String lastCommitId = head.getCommitId();
        String commitId = generateTree();
        String message = opString02;
        String time = String.valueOf(System.currentTimeMillis());

        // update headFile
        commitSuccess = MyUtil.updateHead(commitId);
        generateCommit(lastCommitId, commitId, message, time);
        ois.close();
        return commitSuccess;
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
        String commitId = "";
        while (countNumber >= 0) {
            Tree tree = new Tree();
            for (String[] strArr : als) {
                if (countNumber == strArr.length) {
                    tree.addBlob(strArr[countNumber], index.getValue(strArr[countNumber]));
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
            FileOutputStream fos = new FileOutputStream(objPath + sep + treeHash);
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
        String prePath = System.getProperty("user.dir");
        String objPath = MyUtil.getObjPath(prePath);
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

    // corgit --help
    public static void gitHelp() {
        MyUtil.printHelpDoc();
    }

    // corgit ls-files --staged
    public static void corgitListIndex(String prePath) {
        try {
            MyUtil.showIndex(prePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
