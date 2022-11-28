import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class corgit {
    public static void main(String[] args) {
        String curPath = System.getProperty("user.dir");
        // jump to different options
        // corgit init
        if (args[0].equals("init") && args.length == 1) {
            corgitInit();
        // corgit add
        } else if (args[0].equals("add") && args.length == 2) {
            if (args[1].equals(".")) {
                corgitAddAll(curPath);
            } else corgitAdd(args[1], curPath);
        // corgit commit
        } else if (args[0].equals("commit") && args.length == 3) {
            corgitCommit(args[1], args[2], curPath);
        // corgit --help
        } else if (args[0].equals("--help") && args.length == 1) {
            corgitHelp();
        // corgit ls-files --stage
        } else if (args[0].equals("ls-files") && 
                   args[1].equals("--stage") && args.length == 2) {
            corgitListIndex(curPath);
        // corgit ls-head
        } else if (args[0].equals("ls-head") && args.length == 1) {
            corgitShowHead(curPath);
        // corgit rm --cached / rm fileName /rm -r dir
        } else if (args[0].equals("rm")) {
            if (args[1].equals("--cached") && 
            (args.length == 3)) {
                corgitRmCache(args[2], curPath);
            } else if (args[1].equals("-r") && 
            (args.length == 3)) {
                corgitRmDir(args[2], curPath);
            } else if (args.length == 2 &&
            !args[1].equals("--cached") && !args[1].equals("-r"))
             corgitRmFile(args[1], curPath);
             else MyUtil.wrongFormat(args);
        // corgit log
        } else if (args[0].equals("log") && args.length == 1) {
            corgitLog(curPath);
        }
        // wrong instruction, show help
        else {
            MyUtil.wrongFormat(args);
        }
    }

    // corgit init
    public static boolean corgitInit() {
        boolean initSuccess = false;
        /* 
        check whether in .corgit path 
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
    public static boolean corgitAdd(String fileName, String curPath) {
        // check initialized
        if (!MyUtil.checkInitialized(curPath)) {
            System.out.println("fatal: not a corgit repository (or any of the parent directories): .git");
            return false;
        }
        boolean addSuccess = false;
        String sep = File.separator;
        String filePath = curPath + sep +fileName;
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                addSuccess = MyUtil.addToIndex(fileName, curPath);
                addSuccess = MyUtil.generateBlob(fileName, curPath);
            } else if (file.isDirectory()) {
                corgitAddAll(file.getAbsolutePath());
            }
        } else if (!file.exists()) {//#TODO
            // notice: since file does not exsit, filePath may not be a real path
            if (MyUtil.checkFileInIndex(fileName, curPath)) {
                addSuccess = MyUtil.removeFileInIndex(fileName, curPath);
                addSuccess = MyUtil.removeFileDeletedInIndex(curPath + sep + fileName);
            } else {
                System.out.println("fatal: pathspec '" + 
                fileName + "' did not match any files");
            }
        }
        return addSuccess;
    }

    public static boolean corgitAddAll(String curPath) {
        boolean addSuccess = false;
        String sep = File.separator;
        File preDir = new File(curPath);
        File[] files = preDir.listFiles();
        //#TODO
        // if some files were deleted in this dir, remove their items in index file
        addSuccess = MyUtil.removeFileDeletedInIndex(curPath);
        for (File f : files) {
            if (f.isFile()) corgitAdd(f.getName(), curPath);
            if (f.isDirectory()) {
                if(f.getName().equals(".corgit")) continue;
                // System.out.println(curPath + "/" + f.getName());
                String subPath = curPath + sep + f.getName();
                corgitAddAll(subPath);
            }
        }
        addSuccess = true;
        return addSuccess;
    }

    // corgit commit (-m "notes")
    public static boolean corgitCommit(String opString01, String opString02, String curPath) {
        // check initialized
        if (!MyUtil.checkInitialized(curPath)) {
            System.out.println("fatal: not a corgit repository (or any of the parent directories): .git");
            return false;
        }
        // check input format
        if (!opString01.equals("-m")) {
            System.out.println("use 'corgit commit -m ···' and try again.");
        }
        boolean commitSuccess = false;
        try {
            // do commit, read Head file and change it 
            String headPath = MyUtil.getHeadFilePath(curPath);
            FileInputStream fis = new FileInputStream(headPath);
            // System.out.println(headPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Head head = (Head) ois.readObject();

            String lastCommitId = head.getCommitId();
            String commitId = MyUtil.generateTree();
            String message = opString02;
            String time = String.valueOf(System.currentTimeMillis());

            // update headFile
            commitSuccess = MyUtil.updateHead(commitId);
            MyUtil.generateCommit(lastCommitId, commitId, message, time);
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return commitSuccess;
    }




    // corgit --help
    public static void corgitHelp() {
        MyUtil.printHelpDoc();
    }

    // corgit ls-files --staged
    public static void corgitListIndex(String curPath) {
        try {
            MyUtil.showIndex(curPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // print the content of head
    public static void corgitShowHead(String curPath) {
        try {
            MyUtil.showHead(curPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // delete file information in staged area
    public static void corgitRmCache(String fileName, String curPath) {
        if (fileName.equals(null)) {
            corgitHelp();
            return;
        } else {
            MyUtil.deleteToIndex(fileName, curPath);
        }
    }

    //
    public static void corgitRmFile(String fileName, String curPath) {

    }
    // 
    public static void corgitRmDir(String dirName, String curPath) {

    }

    //
    public static void corgitLog(String curPath) {
        
    }

}
