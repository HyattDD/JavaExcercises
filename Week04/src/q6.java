import java.util.Scanner;

public class q6{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int number = sc.nextInt();
        int[] arr = new int[number];
        int index = 0;
        for (int i = 0; i < number; i++) {
            if (i % (1 << 10) == 0) System.gc();
            int temp = sc.nextInt();
            if(arr[temp] == 0) {
                arr[temp] = 1;
                index = i;
            }
        }
        System.out.println(index);
        sc.close();
    }
}
