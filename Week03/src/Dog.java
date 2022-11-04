public class Dog extends Animal{
    public Dog() {
    }

    public Dog(int age, String color) {
        super(age, color);
    }

    @Override
    public void eat (String something) {
        System.out.println(getAge() + "岁的" + getColor() + "的修狗抱住" + something + "狠狠地啃起来");
    }

    public void lookHome () {
        System.out.println("狗在看家");
    }
}
