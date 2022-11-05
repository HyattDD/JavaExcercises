import java.util.ArrayList;
import java.util.Scanner;
/*本程序在最初的crawler程序基础上进行改动：
 * 从某个URL开始搜索某个单词（例如，Computer Programming），程序提示用户输入单词以及起始URL，并且一旦搜索到该单词则终止程序。显示包含该单词的页面URL地址。
 */

public class WebCrawler01 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter a URL:");
        String url = input.nextLine();
        System.out.println("Enter your words:");
        String words = input.nextLine();
        crawler(url, words);
        input.close();
    }

    public static void crawler(String startURL, String words) {
        ArrayList<String> listOfPendingURLs = new ArrayList<>();
        ArrayList<String> listOfTraversedURLs = new ArrayList<>();
        listOfPendingURLs.add(startURL);
        while (!listOfPendingURLs.isEmpty() &&
                listOfPendingURLs.size() <= 100) {
            String urlString = listOfPendingURLs.remove(0);
            if (!listOfTraversedURLs.contains(urlString)) {
                listOfTraversedURLs.add(urlString);
                System.out.println("Crawl " + urlString);
                for (String s : getSubURLs(urlString, words)) {
                    if (!listOfTraversedURLs.contains(s))
                        listOfPendingURLs.add(s);
                }
            }
        }
        // 爬取的最后的一个url中就是包含我们要查找的单词的网页
        System.out.println("Your words has been found in the last url.");
    }

    public static ArrayList<String> getSubURLs(String urlString, String words) {
        ArrayList<String> list = new ArrayList<>();
        try {
            java.net.URL url = new java.net.URL(urlString);
            Scanner input = new Scanner(url.openStream());
            int current = 0;
            while (input.hasNext()) {
                String line = input.nextLine();
                // 判断读取的页面行中是否包含我们要查找饿单词，若包含则返回当前url列表
                if (line.contains(words)) {
                    input.close();
                    return list;
                }
                current = line.indexOf("http:", current);
                while (current > 0) {
                    int endIndex = line.indexOf("\"", current);
                    if (endIndex > 0) {
                        list.add(line.substring(current, endIndex));
                        current = line.indexOf("http:", endIndex);
                    } else current = -1;
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return list;
    }
}
