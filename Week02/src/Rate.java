import java.util.Scanner;
public class Rate {
    public static void main(String[] args) {
        double basicRate = 7.7;
        Scanner sc = new Scanner(System.in);
        int year = sc.nextInt();
        if (year < 0) {
            System.out.println("error");
        }else if (year <= 1) {
            System.out.printf("实际利率=%.2f%%", basicRate * 0.5);
        }else if (year <= 3) {
            System.out.printf("实际利率=%.2f%%", basicRate * 0.7);
        }else if (year <= 5) {
            System.out.printf("实际利率=%.2f%%", basicRate);
        }else {
            System.out.printf("实际利率=%.2f%%", basicRate * 1.1);
        }
        sc.close();
    }
}