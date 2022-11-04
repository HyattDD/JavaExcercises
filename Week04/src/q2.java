import java.util.Scanner;

public class q2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int number = sc.nextInt();
        int[] hashArr = new int[100010];
        int index =0;
        int[] resArr = new int[100010];
        for (int i = 0; i < number; i++) {
            int x  = sc.nextInt();
            if(hashArr[x] !=0) continue;
            hashArr[x] = 1;
            resArr[index++] = x;
        }
        for (int i = 0; i < index-1; i++) {
            System.out.print(resArr[i]+" ");
        }
        System.out.print(resArr[index-1]);
        sc.close();
    }
}
