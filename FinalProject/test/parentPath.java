import java.io.File;

public class parentPath {
    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        File file = new File(path);

        System.out.println(file.getParent());
    }

    public static Boolean findFile(String fileString, String path) {
        boolean find = false;
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                if (f.getName().equals(fileString)) find = true;
            }
            if (f.isDirectory()) {
                find = findFile(fileString, f.getName() + "/");
            }
        }
        return find;
    }

} 
