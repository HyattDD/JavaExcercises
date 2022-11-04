/*
在主程序类中定义两个时钟对象，时钟对象1初始化为（0：0：0）；输入一个时间，初始化时钟对象2，然后输出两个时钟的值。
*/
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MyClock clock1,clock2;


        //输入一个时间：时 分 秒
        Scanner sc = new Scanner(System.in);
        int hour, minute, second;
        System.out.println("输入一个时间：（时 分 秒用空格分隔）");
        String s = sc.nextLine();
        String[] arr = s.split(" ");
        hour = Integer.parseInt(arr[0]);
        minute = Integer.parseInt(arr[1]);
        second = Integer.parseInt(arr[2]);
        clock1 = new MyClock(hour, minute, second);
        //用输入的时间初始化时间对象clock2
        clock2 = new MyClock(clock1);
        //显示两个时间对象
        clock1.display();
        clock2.display();

        sc.close();
    }
}

class MyClock {
    //数据成员: 时 分 秒
    private int hour;
    private int minute;
    private int second;

    //方法成员：构造方法1--不带参数的，初始化时钟为 0：0：0
    public MyClock() {
        this.hour = 0;
        this.minute = 0;
        this.second = 0;
    }
    //方法成员：构造方法2-- 带参数的，三个参数分别（ 时，分，秒）
    public MyClock(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }
    //方法成员：构造方法3-- 用对象作为参数
    MyClock(MyClock clock) {
        this.hour = clock.hour;
        this.minute = clock.minute;;
        this.second = clock.second;
    }
    //方法成员:设置时间
    public void setClock(int hour,int minute,int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }
    //方法成员: 显示时间
    public void display() {
        if (hour < 10 && hour >= 0) System.out.printf(" " + hour + ":");
        else System.out.printf("%d:", hour);
        if (minute < 10 && minute >= 0) System.out.printf(" " + minute + ":");
        else System.out.printf("%d:", minute);
        if (second < 10 && second >= 0) System.out.printf(" " + second + "\n");
        else System.out.printf("%d\n", second);
    }

}
