public class Test {
    public static void main(String[] args) {
        Person p = new Person("老王", 18);
        Dog d = new Dog(2, "黑");
        Cat c = new Cat(3, "白");
        p.keepPet(d, "骨头");
        p.keepPet(c, "小鱼干");
    }
}
