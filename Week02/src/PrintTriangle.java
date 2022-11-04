// 打印一个空心三角形
import java.util.Scanner;

public class PrintTriangle {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int line = sc.nextInt();
        if (line == 1) {
            System.out.print("*");
            sc.close();
            return;
        }
        // 数字是几，总共打几行
        for (int i = 0; i < line; i++) {
            // 第一行在正中间打一个星星
            if (i == 0) {
                for (int j = 0; j < line - 1; j++) {
                    System.out.print(" ");
                }
                System.out.print("*");
                for (int j = 0; j < line - 1; j++) {
                    System.out.print(" ");
                }
                System.out.println();
                continue;
            }
            // 最后一行打满星星
            if (i == line - 1) {
                for (int j = 0; j < line * 2 - 1; j++) {
                    System.out.print("*");
                }
                break;
            }
            // 中间行先打空格
            for (int j = 0; j < line - (i + 1); j++){
                System.out.print(" ");
            }
            // 再打一个星星
            System.out.print("*");
            // 再打空格
            for (int j = 0; j < (i - 1) * 2 + 1; j++){
                System.out.print(" ");
            }
            // 再打星星
            System.out.print("*");
            // 再打空格，然后换行
            for (int j = 0; j < line - (i + 1); j++){
                System.out.print(" ");
            }
            System.out.println();
        }
        sc.close();
    }
}
