package Dictionary;

public class TrieTree {

    private final int SIZE = 26;    //每个节点能包含的子节点数，即需要SIZE个指针来指向其孩子
    private Node root;              //字典树的根节点


    /**
     *  @author: Ragty
     *  @Date: 2020/2/24 21:10
     *  @Description: 字典树节点
     */
    private class Node {
        private boolean status;   //标识该节点是否为某一字符串终端节点
        private Node[] child;     //该节点的子节点

        public Node() {
            child = new Node[SIZE];
            status = false;
        }
    }



    /**
    * @Author Ragty
    * @Description 初始化一个节点
    * @Date   2020/2/24 21:11
    */
    public TrieTree() {
        root = new Node();
    }



    /**
     * @Author Ragty
     * @Description 在字典树中插入一个单词
     * @Date   2020/2/23 21:42
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        Node pNode = this.root;
        for (int i = 0; i < word.length(); i++)
        {
            int index = word.charAt(i) - 'a';
            if (pNode.child[index] == null) {   //如果不存在节点，则新建一个节点插入
                Node tmpNode = new Node();
                pNode.child[index] = tmpNode;
            }
            pNode = pNode.child[index];         //指向下一层
        }
        pNode.status = true;
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/2/24 21:15
     *  @Description: 检查字典树中是否完全包含字符串
     */
    public boolean hasStr(String word) {
        Node pNode = this.root;

        //逐个字符去检查
        for (int i = 0; i < word.length(); i++) {
            int index = word.charAt(i) - 'a';
            //在字典树中没有对应的节点，或者word字符串的最后一个字符在字典树中检测对应节点的isStr属性为false，则返回false
            if (pNode.child[index] == null
                    || (i + 1 == word.length() && pNode.child[index].status == false)) {
                return false;
            }
            pNode = pNode.child[index];
        }

        return true;
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/2/24 21:21
     *  @Description: 先序遍历
     */
    public void preWalk(Node root) {
        Node pNode = root;
        for (int i = 0; i < SIZE; i++) {
            if (pNode.child[i] != null) {
                System.out.print((char) ('a' + i) + "--");
                preWalk(pNode.child[i]);
            }
        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/2/24 21:17
     *  @Description: 返回字典树的根节点
     */
    public Node getRoot() {
        return root;
    }


    public static void main(String[] args) {
        TrieTree trieTree = new TrieTree();
        trieTree.insert("sad");
        trieTree.insert("say");
        trieTree.insert("to");
        trieTree.insert("too");

        System.out.println(trieTree.hasStr("say"));
        System.out.println(trieTree.hasStr("toooo"));

        Node root = trieTree.getRoot();
        trieTree.preWalk(root);

    }

}