package tools.spring.AOP.base;

public class ReflectTest {
    public static void main(String[] args) {
        //注意一定要返回接口，不能返回实现类否则会报错
        Fruit fruit = (Fruit) DynamicAgent.agent(Fruit.class, new Apple());
        fruit.show();
    }
}
