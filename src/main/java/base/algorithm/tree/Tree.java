package base.algorithm.tree;

public class Tree {


    /**
     * 最大深度
     * @param node
     * @return
     */
    static int getMaxDeath(TreeNode node){
        if(node==null){
            return 0;
        }
        int left = getMaxDeath(node.left);
        int right = getMaxDeath(node.right);
        return Math.max(left,right) + 1;
    }

    /**
     * 最小深度
     * @param root
     * @return
     */
    static int getMinDepth(TreeNode root){
        if(root == null){
            return 0;
        }
        int left=getMinDepth(root.left);
        int right=getMinDepth(root.right);
        if (left==0) return right+1;
        if (right==0) return left+1;
        return Math.min(left,right) + 1;
    }

    /**
     * 节点个数
     * @param root
     * @return
     */
    static int numOfTreeNode(TreeNode root){
        if(root == null){
            return 0;

        }
        int left = numOfTreeNode(root.left);
        int right = numOfTreeNode(root.right);
        return left + right + 1;
    }

    static int numsOfNoChildNode(TreeNode root){
        if(root == null){
            return 0;
        }
        if(root.left==null&&root.right==null){
            return 1;
        }
        return numsOfNoChildNode(root.left)+numsOfNoChildNode(root.right);

    }

    static int numsOfkLevelTreeNode(TreeNode root,int k){
        if(root == null||k<1){
            return 0;
        }
        if(k==1){
            return 1;
        }
        int numsLeft = numsOfkLevelTreeNode(root.left,k-1);
        int numsRight = numsOfkLevelTreeNode(root.right,k-1);
        return numsLeft + numsRight;
    }

    public static void main(String[] args) {
        TreeNode node1=new TreeNode(1);
        TreeNode node2=new TreeNode(2);
        TreeNode node3=new TreeNode(3);
        TreeNode node4=new TreeNode(4);
        TreeNode node5=new TreeNode(5);
        TreeNode node6=new TreeNode(6);
        TreeNode node7=new TreeNode(7);
        node1.left=node2;
        node1.right=node3;
        node2.left=node4;

        System.out.println(getMinDepth(node1));
        System.out.println(getMaxDeath(node1));
        System.out.println(numOfTreeNode(node1));
        System.out.println(numsOfNoChildNode(node1));
        System.out.println(numsOfkLevelTreeNode(node1,3));
    }

}
