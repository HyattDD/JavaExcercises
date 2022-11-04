import java.util.Scanner;

public class Prime {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int number = sc.nextInt();
        double cell = Math.sqrt(number);
        boolean isPrime = number != 1;
        if (number % 2 == 0 && number != 2) {
            isPrime = false;
        }
        for (int i = 2; i <= cell; i+=2) {
            if (number % i == 0) {
                isPrime = false;
                break;
            }
        }
        if (isPrime) {
            System.out.println(number + " is a prime number.");
        }else {
            System.out.println(number + " is not a prime number.");
        }
        sc.close();
    }
}