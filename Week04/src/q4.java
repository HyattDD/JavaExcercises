import java.util.ArrayList;
import java.util.Scanner;

public class q4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int number = sc.nextInt();
        ArrayList<Integer> arr = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < number; i ++) {
            int tempIn = sc.nextInt();
            if (!arr.contains(tempIn)) {
                arr.add(tempIn);
                index = i;
            }
            if (i % (1<<16) == 0) System.gc();
        }
        System.out.println(index);
        sc.close();
    }
    
}
