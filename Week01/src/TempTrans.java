import java.util.Scanner;

public class TempTrans {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double F = sc.nextDouble();
        double C;
        C = 5.0 * (F - 32) / 9.0;
        System.out.printf("%.2f",C);
        sc.close();
    }
}
