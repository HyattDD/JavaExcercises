import java.util.Scanner;

public class BMI {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double weight = sc.nextDouble();
        double height = sc.nextDouble();
        double BMI = weight / (height * height);
        if (weight > 727 || weight <= 0 || height > 2.72 || height <= 0){
            System.out.println("input out of range");
        } else if (BMI < 18.5) {
            System.out.println("thin");
        } else if (BMI >= 18.5 && BMI < 24) {
            System.out.println("fit");
        } else if (BMI >=24 && BMI <28) {
            System.out.println("overweight");
        } else System.out.println("fat");
        sc.close();
    }
}
