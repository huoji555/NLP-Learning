package Dictionary;

import com.hankcs.hanlp.algorithm.ahocorasick.trie.Emit;

import java.util.*;

public class AhoCorasickTrie {

    private Boolean failureStatesConstructed = false;   //是否建立了failure表
    private Node root;                                  //根结点


    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 13:49
     *  @Description: ACTire初始化
     */
    public AhoCorasickTrie() {
        this.root = new Node(true);
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 13:54
     *  @Description: ACTrie节点(内部用字典树构建)
     *
     */
    private static class Node{
    private Map<Character, Node> map;
    private List<String> emits;         //输出
    private Node failure;               //失败中转
    private Boolean isRoot = false;     //是否为根结点


    public Node(){
        map = new HashMap<>();
        emits = new ArrayList<>();
    }


    public Node(Boolean isRoot) {
        this();
        this.isRoot = isRoot;
    }


    public Node insert(Character character) {
        Node node = this.map.get(character);
        if (node == null) {
            node = new Node();
            map.put(character, node);
        }
        return node;
    }


        public void addEmit(String keyword) {
            emits.add(keyword);
        }


        public void addEmit(Collection<String> keywords) {
            emits.addAll(keywords);
        }


        /**
         *  @author: Ragty
         *  @Date: 2020/4/1 14:22
         *  @Description: success跳转
         */
        public Node find(Character character) {
            return map.get(character);
        }


        /**
         *  @author: Ragty
         *  @Date: 2020/4/1 14:23
         *  @Description: 状态转移(此处的transition为转移的状态，可理解为接收的一个词)
         */
        private Node nextState(Character transition) {
            Node state = this.find(transition);             //先按success跳转

            if (state != null) {
                return state;
            }

            if (this.isRoot) {                              //如果跳转到根结点还是失败，则返回根结点
                return this;
            }

            return this.failure.nextState(transition);      // 跳转失败，按failure跳转
        }


        public Collection<Node> children() {
            return this.map.values();
        }


        public void setFailure(Node node) {
            failure = node;
        }


        public Node getFailure() {
            return failure;
        }


        public Set<Character> getTransitions() {
            return map.keySet();
        }


        public Collection<String> emit() {
            return this.emits == null ? Collections.<String>emptyList() : this.emits;
        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 15:01
     *  @Description: 模式串(用于模式串匹配)
     */
    private static class Emit{
        private final String keyword;   //匹配到的模式串
        private final int start;        //起点
        private final int end;          //终点

        public Emit(final int start, final int end, final String keyword) {
            this.start = start;
            this.end = end;
            this.keyword = keyword;
        }

        public String getKeyword() {
            return this.keyword;
        }

        @Override
        public String toString() {
            return super.toString() + "=" + this.keyword;
        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 15:10
     *  @Description: 添加一个模式串(内部使用字典树构建)
     */
    public void addKeyword(String keyword) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }

        Node currentState = this.root;
        for (Character character : keyword.toCharArray()) {
            currentState = currentState.insert(character);
        }
        currentState.addEmit(keyword);          //记录完整路径的output表(第一步)
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 17:43
     *  @Description: 模式匹配
     */
    public Collection<Emit> parseText(String text) {
        checkForConstructedFailureStates();
        Node currentState = this.root;
        List<Emit> collectedEmits = new ArrayList<>();
        for (int position = 0; position < text.length(); position++) {
            Character character = text.charAt(position);
            currentState = currentState.nextState(character);
            Collection<String> emits = currentState.emit();
            if (emits == null || emits.isEmpty()) {
                continue;
            }
            for (String emit : emits) {
                collectedEmits.add(new Emit(position - emit.length() + 1, position, emit));
            }
        }
        return collectedEmits;
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 16:04
     *  @Description: 建立Fail表(核心,BFS遍历)
     */
    private void constructFailureStates() {
        Queue<Node> queue = new LinkedList<>();

        for (Node depthOneState : this.root.children()) {
            depthOneState.setFailure(this.root);
            queue.add(depthOneState);
        }
        this.failureStatesConstructed = true;

        while (!queue.isEmpty()) {
            Node parentNode = queue.poll();
            for (Character transition : parentNode.getTransitions()) {
                Node childNode = parentNode.find(transition);
                queue.add(childNode);
                Node failNode = parentNode.getFailure().nextState(transition);   //在这里构建failNode
                childNode.setFailure(failNode);
                childNode.addEmit(failNode.emit());                             //用路径后缀构建output表(第二步)
            }
        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/4/1 15:28
     *  @Description: 检查是否建立了Fail表(若没建立，则建立)
     */
    private void checkForConstructedFailureStates() {
        if (!this.failureStatesConstructed) {
            constructFailureStates();
        }
    }




    public static void main(String[] args) {
        AhoCorasickTrie trie = new AhoCorasickTrie();
        trie.addKeyword("hers");
        trie.addKeyword("his");
        trie.addKeyword("she");
        trie.addKeyword("he");

        Collection<Emit> emits = trie.parseText("ushers");
        for (Emit emit : emits) {
            System.out.println(emit.start + " " + emit.end + "\t" + emit.getKeyword());
        }
    }


}
