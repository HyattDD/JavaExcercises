import java.util.Scanner;

public class q1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int len = sc.nextInt();
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = sc.nextInt();
        }
        int moveTimes = sc.nextInt();
//        System.out.println(moveTimes);
        while (moveTimes > 0) {
            int tempIndex = sc.nextInt();
            move(arr, tempIndex);
            moveTimes -= 1;
        }
        for (int i = 0; i < arr.length; i++) {
            System.out.printf("%d", arr[i]);
            if (i < (arr.length - 1)) {
                System.out.print(" ");
            }
        }
        sc.close();
    }

    public static void move(int[] arr, int index) {
        int temp = arr[index - 1];
        for (int i = index; i < arr.length; i++) {
            arr[i - 1] = arr[i];
        }
        arr[arr.length - 1] = temp;
    }
}
