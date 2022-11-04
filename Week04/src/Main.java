import java.util.HashSet;
import java.util.Scanner;
public class Main
{
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        int number = 0;
        number = sc.nextInt();
        int[] arr = new int[number];
        for(int i = 0; i < number; i++)
        {
            arr[i] = sc.nextInt();
            if(i%(1<<12) == 0) System.gc();
        }
        HashSet<Integer> set = new HashSet<Integer>();
        int index = 0;
        int count1 = 0, count2 = 0;
        for(int elem : arr)
        {
            if(set.add(elem)) count1 ++;
        }
        set.clear();
        for(int elem : arr)
        {
            if(count1 == count2) break;
            if(set.add(elem)) count2++;
            index ++;
        }
        System.out.println(index - 1);
        sc.close();
    }
}