import java.io.File;

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
        // corgit reset
        } else if (args[0].equals("reset") && args.length == 3) {
            corgitReset(args[1], args[2], curPath);
        // corgit push
        } else if (args[0].equals("push") && args.length == 1) {
            corgitPush(curPath);
        } else if (args[0].equals("pull") && args.length == 1) {
            corgitPull(curPath);
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
            Head head = (Head) MyUtil.readHead(curPath);
            String lastCommitId = head.getCommitId();
            String treeId = MyUtil.generateTree();
            // System.out.println("corgitCommit : treeId: " + treeId);
            String commitId = MyUtil.getHashOfByteArray(treeId.getBytes());
            String message = opString02;
            String time = MyUtil.getTime();
            // update headFile
            commitSuccess = MyUtil.updateHead(commitId);
            MyUtil.generateCommit(lastCommitId, commitId, treeId, message, time);
        } catch (Exception e) {
            System.out.println("fail to commit this time");
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
            System.out.println("fail to get the content of index");
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
    public static boolean corgitLog(String curPath) {
        boolean logSuccess = false;
        // check initialized
        if (MyUtil.notInitialized(curPath)) return logSuccess;
        Log log = new Log();
        if (log.getOriginHeadContent().isEmpty()) {
            System.out.println("fatal: your current repo does not have any commits yet");
            return logSuccess;
        } else {
            logSuccess = log.log(curPath, true);
        }
        return logSuccess;
    }


    // corgit push
    public static boolean corgitPush(String curPath) {
        boolean pushSuccess = false;
        // check initialized
        if (MyUtil.notInitialized(curPath)) return pushSuccess;
        System.out.println("[Default] Push will mkdirs by relative path'../remoteRepo' of your working space");
        String sep = File.separator;
        File workingSpace = new File(curPath);
        String remoteRepoPath = workingSpace.getParent() + sep + "remoteRepo";
        File remoteRepo = new File(remoteRepoPath);
        remoteRepo.mkdirs();
        int count = 0;
        try {
            count = ZipUtil.doZip(curPath, "push.zip");
            MyServer myServer = new MyServer(remoteRepoPath + sep + "push.zip");
            MyClient myClient = new MyClient(curPath + sep + "push.zip");
            Thread serverThread = new Thread(myServer);
            Thread clientThread = new Thread(myClient);
            serverThread.start();
            clientThread.start();
            // to ensure that the threads all finished
            serverThread.join();
            clientThread.join();
        } catch (Exception e) {
            System.out.println("Sorry, corgit failed while pushing");
            return pushSuccess;
        } 
        File pushFile = new File(curPath + sep + "push.zip");
        pushFile.delete();
        System.out.println(count + " files were packed in total");
        System.out.println("[Default] Push done.");
        pushSuccess = true;
        return pushSuccess;
    }

    // corgit pull
    public static boolean corgitPull(String curPath) {
        boolean pullSuccess = false;
        // check initialized
        if (MyUtil.notInitialized(curPath)) return pullSuccess;
        System.out.println("[Default] Pull will get files from relative path'../remoteRepo' of your working space");
        String sep = File.separator;
        File workingSpace = new File(curPath);
        String remoteRepoPath = workingSpace.getParent() + sep + "remoteRepo";
        File remotePushFile = new File(remoteRepoPath + sep + "push.zip");
        int count = 0;
        if (!remotePushFile.exists()) {
            System.out.println("No commit in remote repository, try push");
            return pullSuccess;
        }
        try {
            // now server is in local working space, client is remote repo
            MyServer myServer = new MyServer(curPath + sep + "pull.zip");
            MyClient myClient = new MyClient(remotePushFile.getAbsolutePath());
            Thread serverThread = new Thread(myServer);
            Thread clientThread = new Thread(myClient);
            serverThread.start();
            clientThread.start();
            serverThread.join();
            clientThread.join();
        } catch (Exception e) {
            System.out.println("Sorry, corgit failed while pulling");
            return pullSuccess;
        }
        System.out.println("unzipping pulled file..."); 
        count = ZipUtil.unzip(curPath, "pull.zip"); 
        File pullFile = new File(curPath + sep + "pull.zip");
        pullFile.delete();
        System.out.println(count + " files were unpacked in total");
        System.out.println("[Default] Pull done.");
        pullSuccess = true;
        return pullSuccess;
    }


    public static boolean corgitReset(String optionStr, String commitId, String curPath) {
        boolean resetSuccess = false;
        Reset reset = new Reset(commitId);
        if (optionStr.equals("--soft")) reset.resetSoft(curPath);
        if (optionStr.equals("--mixed")) reset.resetMixed(curPath);
        if (optionStr.equals("--hard")) reset.resetHard(curPath);
        return resetSuccess;
    }
}
