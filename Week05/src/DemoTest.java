class Demo {
    String temp = "hello";
}

public class DemoTest {
    public static void main(String[] args) {
        Demo d1 = new Demo();
        d1.temp = "world";
        System.out.println(d1.temp + " ");
        fun(d1);
        System.out.println(d1.temp);
    }

    public static void fun(Demo d2) {
        d2.temp = "MLDN";

    }
}
