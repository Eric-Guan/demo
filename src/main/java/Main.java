import java.util.concurrent.*;

public class Main {
    public static void main(String[]args){
//        doTaskWithResultInWorker();
        System.out.println(Math.round(-1.5));
    }

    public int GetNumberOfK(int [] array , int k) {
        int count=0;
        for (int i = 0; i < array.length; i++) {
            if (array[i]==k) count ++;
        }
        return count;
    }

    public static int run(TreeNode root) {
        if(root==null) return 0;
        int left = run(root.left);
        int right = run(root.right);
        if (left==0) return right+1;
        if (right==0) return left+1;
        return Math.min(left,right) + 1;
    }

    private static void doTaskWithResultInWorker() {
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("Task starts");
                Thread.sleep(1000);
                int result = 0;
                for (int i=0; i<=100; i++) {
                    result += i;
                }
                System.out.println("Task finished and return result");
                return result;
            }
        };
        FutureTask<Integer> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();

        try {
            System.out.println("Before futureTask.get()");
            System.out.println("Result:" + futureTask.get(1, TimeUnit.SECONDS));
            System.out.println("After futureTask.get()");
        } catch (InterruptedException e) {
            System.out.println(1);
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.out.println(2);
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println(3);
            e.printStackTrace();
        }
    }

}

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
}