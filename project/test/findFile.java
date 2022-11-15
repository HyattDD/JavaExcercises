import java.io.File;


// 测试findFile函数
public class findFile {
    public static void main(String[] args) {
        String property = System.getProperty("user.dir");
        System.out.println(property);
        boolean flag = find("Copy.java", property);
        System.out.println(flag);
    }
    public static Boolean find(String fileString, String path) {
        boolean find = false;
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                //Do something
                if (f.getName().equals(fileString)) find = true;
            }
            if (f.isDirectory()) {
                find = find(fileString, f.getName() + "/");
            }
        }
        return find;
    }
    
}
