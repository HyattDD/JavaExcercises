public class Cat extends Animal{
    public Cat() {
    }

    public Cat(int age, String color) {
        super(age, color);
    }

    @Override
    public void eat(String something) {
        System.out.println(getAge() + "岁的" + getColor() + "的猫咪抓住" + something + "快乐地吃起来");
    }

    public void catchMouse () {
        System.out.println("猫抓老鼠");
    }
}
