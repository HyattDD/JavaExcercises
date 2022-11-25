import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
                gitCommit(args[1], args[2]);
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
    public static void gitCommit(String opString01, String opString02) throws Exception {
        if (!opString01.equals("-m")) {
            System.out.println("use 'corgit commit -m ···' and try again.");
        }
        String prePath = System.getProperty("user.dir");
        String indexPath = MyUtil.getDotCorgitPath(prePath) + "/index";

        String lastCommitId = "";
        String commitId = generateTree(indexPath);
        String message = opString02;
        String time = String.valueOf(System.currentTimeMillis());
        generateCommit(lastCommitId, commitId, message, time);
    }





    // update index file when git add
    public static void updateIndex(String fileName, String prePath) {
        String dotCorgitPath = MyUtil.getDotCorgitPath(prePath);
        try (
            FileInputStream file = new FileInputStream(dotCorgitPath + "/index");
        ) {
            Index index = (Index) new ObjectInputStream(file).readObject();
            String hashText = MyUtil.getHashOfFile(prePath + "/" +fileName);
            index.setItem(fileName, hashText);
            // System.out.println(index.getValue(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // generate tree object when commit
    public static String generateTree(String indexPath) throws Exception{
        
        FileInputStream file = new FileInputStream(indexPath);
        Index index = (Index) new ObjectInputStream(file).readObject();
        Tree tree = new Tree();
        tree.getIndex(index.getMap());

        String indexContent = index.toString();
        String hashText = MyUtil.getHashOfByteArray(indexContent.getBytes());

        File treeDir = new File(MyUtil.getObjPath(indexPath) + "/" + hashText.substring(0, 2));
        treeDir.mkdir();
        File desfile = new File(treeDir + "/" + hashText.substring(2, 40));
        ObjectOutputStream output = 
        new ObjectOutputStream(new FileOutputStream(desfile));
        output.writeObject(tree);

        file.close();
        output.close();

        System.out.println("DEBUG: Object tree has been written down in objects directory\n");
        return hashText.substring(2, 40);
    }

    // generate commit object when commit
    public static void generateCommit(String lastCommitId, String commitId, String message, String time) throws Exception {
        Commit commit = new Commit(lastCommitId, commitId, message, time);
        String content = commit.toString();
        String prePath = System.getProperty("user.dir");
        String objPath = MyUtil.getObjPath(prePath);
        String hashText = MyUtil.getHashOfByteArray(content.getBytes());
        File commitDir = new File(objPath + hashText.substring(0, 2));
        commitDir.mkdir();
        File desfile = new File(commitDir + "/" + hashText.substring(2, 40));

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
