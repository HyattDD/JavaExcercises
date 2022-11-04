import java.util.Scanner;

public class HellenFormula {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()){
            double a = sc.nextDouble();
            double b = sc.nextDouble();
            double c = sc.nextDouble();
            if ((a+b>c) && (b+c>a) && (a+c>b)){
                double p = getParameter(a, b, c);
                double result = getArea(a, b, c, p);
                System.out.printf("%.2f\n",result);
            }else System.out.println("Input Error!");
        }
        sc.close();
    }

    public static double getParameter(double a, double b, double c) {
        return (a + b + c) / 2.0;
    }

    public static double getArea(double a, double b, double c, double p) {
        return Math.sqrt(p * (p - a) * (p - b) * (p - c));
    }
}
