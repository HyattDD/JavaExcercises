import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class corgit {
    public static void main(String[] args) {
        String curPath = System.getProperty("user.dir");
        /* 
        $ jump to different options, use length to ensure the format
        */
        // corgit init
        if (args[0].equals("init") && args.length == 1) {
            corgitInit(curPath);
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
        // corgit rm 
        } else if (args[0].equals("rm")) {
            // corgit rm --cached
            if (args[1].equals("--cached")) {
                // two sides: rm --cached fileName ; rm --cached dirName
                if (args.length == 3) {
                    corgitRmCache(args[2], curPath, "file");
                } else if ((args.length == 4) && (args[2].equals("-r"))) {
                    corgitRmCache(args[3], curPath, "dir");
                }
            // corgit rm -r
            } else if (args.length == 3 && args[1].equals("-r")) {
                corgitRm(args[2], curPath, "dir");
            } else if (args.length == 2 ) {
                corgitRm(args[1], curPath, "file");
            }
            // wrong rm format
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
    public static boolean corgitInit(String curPath) {
        boolean initSuccess = false;
        /* 
        check whether in .corgit path 
        - if in , fail to init and return
        - if not , check whether present path had been initialized before
            - if yes, reinitialized and make dir .corgit clean
            - if not, initialized
        */ 
        if (MyUtil.checkPathInDotCorgit(curPath)) {
            return initSuccess;
        } else return MyUtil.initCorgit();
    }


    // corgit add
    public static boolean corgitAdd(String fileName, String curPath) {
        // check initialized
        if (MyUtil.notInitialized(curPath)) return false;
        // add
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
                addSuccess = MyUtil.deleteToIndex(fileName, curPath);
                addSuccess = MyUtil.removeFileDeletedInIndex(curPath + sep + fileName);
            } else {
                System.out.println("fatal: pathspec '" + 
                fileName + "' did not match any files");
            }
        }
        return addSuccess;
    }

    public static boolean corgitAddAll(String curPath) {
        // check initialized
        if (MyUtil.notInitialized(curPath)) return false;
        // add all
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
        if (MyUtil.notInitialized(curPath)) return false;
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
            String treeId = MyUtil.generateTree();
            // System.out.println("corgitCommit : treeId: " + treeId);
            String commitId = MyUtil.getHashOfByteArray(treeId.getBytes());
            String message = opString02;
            String time = String.valueOf(System.currentTimeMillis());

            // update headFile
            commitSuccess = MyUtil.updateHead(commitId);
            MyUtil.generateCommit(lastCommitId, commitId, treeId, message, time);
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
        // check initialized
        if (MyUtil.notInitialized(curPath)) return;
        try {
            MyUtil.showIndex(curPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // print the content of head
    public static void corgitShowHead(String curPath) {
        // check initialized
        if (MyUtil.notInitialized(curPath)) return;
        MyUtil.showHead(curPath, true);
    }

    // delete file information in staged area
    public static boolean corgitRmCache(String fileName, String curPath, String type) {
        boolean removeSuccess = false;
        // check initialized
        if (MyUtil.notInitialized(curPath)) return removeSuccess;
        // we don't care about whether the file exists in working space
        if (type.equals("file")) {
            if (!MyUtil.checkFileInIndex(fileName, curPath)) {
                if (MyUtil.getNameAppearedTimesInIndex(fileName, curPath) > 0) {
                    System.out.println("fatal: not removing '" + fileName + "' recursively without -r");
                    return removeSuccess;
                } else {
                    System.out.println("fatal: pathspec '" + fileName + "' did not match any files");
                    return removeSuccess;
                } 
            } else {
                removeSuccess = MyUtil.deleteToIndex(fileName, curPath);
                if (removeSuccess) System.out.println("rm : " + MyUtil.getFileRelativePath(fileName, curPath));
                // file not in index but filename appeared, then the file must be a dir
            } 
        } else {
            // we can also use "-r" to delete file item in index
            if (MyUtil.checkFileInIndex(fileName, curPath)) {
                removeSuccess = MyUtil.deleteToIndex(fileName, curPath);
                if (removeSuccess) System.out.println("rm : " + MyUtil.getFileRelativePath(fileName, curPath));
                return removeSuccess;
            } else if (MyUtil.getNameAppearedTimesInIndex(fileName, curPath) > 0) {
                removeSuccess = MyUtil.removeNameContainsInIndex(fileName, curPath);
            } else {
                System.out.println("fatal: pathspec '" + fileName + "' did not match any files");
            }
        }        
        return removeSuccess;
    }

    // delete file both from working space and staged area
    public static boolean corgitRm(String fileName, String curPath, String type) {
        boolean removeSuccess = false;
        // check initialized
        if (MyUtil.notInitialized(curPath)) return removeSuccess;
        String sep = File.separator;
        File file = new File(curPath + sep + fileName);

        // file just mean that user use the command "rm filename" without "-r"
        // maybe the filename is a dirname so we should judge it
        if (type.equals("file")) {
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                    corgitRmCache(fileName, curPath, "file");
                    removeSuccess = true;
                } else {
                    System.out.println("fatal: not removing '" + fileName + "' recursively without -r");
                }
            } else corgitRmCache(fileName, curPath, "file");
        } else {
            if (file.exists()) {
                file.delete();
                corgitRmCache(fileName, curPath, "dir");
                removeSuccess = true;
            } else corgitRmCache(fileName, curPath, "dir");
        }
        return removeSuccess;
    }

    // print commit log
    public static void corgitLog(String curPath) {
        
    }

}
