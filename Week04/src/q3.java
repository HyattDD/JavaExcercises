import java.util.Scanner;

public class q3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int len = Integer.parseInt(sc.nextLine());
        int[] arr = new int[len];
        int sum = 0;
        for (int i = 0; i < len; i++) {
            arr[i] = sc.nextInt();
            sum += arr[i];
        }
        System.out.println(sum);
        sc.close();
    }
}