import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class corgit {
    public static void main(String[] args) {
        try {
            // jump to different options
            if (args[0].equals("init")) {
                corgitInit();
            } else if (args[0].equals("add")) {
                if (args[1].equals(".")) {
                    corgitAddAll(System.getProperty("user.dir"));
                } else corgitAdd(args[1]);
            } else if (args[0].equals("commit")) {
                gitCommit(args[1], args[2]);
            } else if (args[0].equals("--help")) {
                gitHelp();
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
    public static void corgitAdd(String fileName) {
        String prePath = System.getProperty("user.dir");
        String sep = File.separator;
        File file = new File(prePath + sep + fileName);
        if (!file.exists()) {
            System.out.println("fatal: pathspec '" + fileName + "' did not match any files");
            return;
        }
        String dotGitpath = MyUtil.getDotCorgitPath(prePath);
        try {
            updateIndex(fileName, prePath, dotGitpath);
            generateBlob(fileName, prePath, dotGitpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // function overload, just for being called in addAll, because
    public static void corgitAdd(String fileName, String prePath) {
        String sep = File.separator;
        File file = new File(prePath + sep + fileName);
        if (!file.exists()) {
            System.out.println("fatal: pathspec '" + fileName + "' did not match any files");
            return;
        }
        String dotGitpath = MyUtil.getDotCorgitPath(prePath);
        try {
            updateIndex(fileName, prePath, dotGitpath);
            generateBlob(fileName, prePath, dotGitpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void corgitAddAll(String prePath) {
        // System.out.println("now user dir is:" + System.getProperty("user.dir"));
        String sep = File.separator;
        File preDir = new File(prePath);
        File[] files = preDir.listFiles();
        for (File f : files) {
            if (f.isFile()) corgitAdd(f.getName(), prePath);
            if (f.isDirectory()) {
                if(f.getName().equals(".corgit")) continue;
                // System.out.println(prePath + "/" + f.getName());
                String subPath = prePath + sep + f.getName();
                corgitAddAll(subPath);
            }
        }
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



    // generate blob object and write in
    public static void generateBlob(String fileName, String srcPath, String dotcorgitPath) throws Exception{
        System.out.println("DEBUG: generating blob of " + fileName);
        byte[] content = MyUtil.readFileByBytes(srcPath + "/" + fileName);
        // get SHA-1 value
        String hashtext = MyUtil.getHashOfByteArray(content);
        String objPath = new String(dotcorgitPath + "/objects/");
        File blobDir = new File(objPath + hashtext.substring(0, 2));
        blobDir.mkdir();
        System.out.println(blobDir);
        File desfile = new File(blobDir + "/" + hashtext.substring(2, 40));
        try (
            ObjectOutputStream output = 
            new ObjectOutputStream(new FileOutputStream(desfile))
        ) {
            Blob blob = 
            new Blob(content, MyUtil.getOccupyOfByteArray(content), hashtext);
            output.writeObject(blob);
            System.out.println("DEBUG: Object blob has been written down in objects directory ->" + fileName);
            
        } 
    }

    // update index file when git add
    public static void updateIndex(String fileName, String prePath, String dotGitPath) {
        try (
            FileInputStream file = new FileInputStream(dotGitPath + "/index");
        ) {
            Index index = (Index) new ObjectInputStream(file).readObject();
            String hashText = MyUtil.getHashOfFile(prePath + "/" +fileName);
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

    // generate tree object when commit
    public static String generateTree(String indexPath) throws Exception{
        
        FileInputStream file = new FileInputStream(indexPath);
        Index index = (Index) new ObjectInputStream(file).readObject();
        Tree tree = new Tree();
        tree.getIndex(index.getMap());

        String indexContent = index.getContent();
        String hashText = MyUtil.getHashOfByteArray(indexContent.getBytes());

        File treeDir = new File(MyUtil.getObjPath(indexPath) + "/" + hashText.substring(0, 2));
        treeDir.mkdir();
        File desfile = new File(treeDir + "/" + hashText.substring(2, 40));
        ObjectOutputStream output = 
        new ObjectOutputStream(new FileOutputStream(desfile));
        output.writeObject(tree);

        file.close();
        output.close();

        System.out.println("DEBUG: Object tree has been written down in objects directory");
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
            System.out.println("DEBUG: Commit has been written down in objects directory ");
        } 
    }













    // corgit --help
    public static void gitHelp() {
        MyUtil.printHelpDoc();
    }



}
