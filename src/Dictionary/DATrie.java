package Dictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DATrie {
    private final int ARRAY_SIZE = 655350;  //数组大小
    private final int BASE_ROOT = 1;        //base根节点状态
    private final int BASE_NULL = 0;        //base空闲状态
    private final int CHECK_ROOT = -1;      //check根节点状态
    private final int CHECK_NULL = -2;      //check空闲状态
    private TrieNode base[];
    private int check[];


    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 16:05
     *  @Description: DATrie节点
     */
    public class TrieNode {
        private int transferRatio; //转移基数
        private boolean isLeaf = false; //是否为叶子节点
        private Character label = null; //节点标识即插入的字符本身
        private int value = -1; //当该节点为叶子节点时关联的字典表中对应词条的索引号

        public int getTransferRatio() {
            return transferRatio;
        }

        public void setTransferRatio(int transferRatio) {
            this.transferRatio = transferRatio;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public void setLeaf(boolean leaf) {
            isLeaf = leaf;
        }

        public Character getLabel() {
            return label;
        }

        public void setLabel(Character label) {
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 16:06
     *  @Description: 构造DATrie
     */
    public void build(List<String> words) {
        init();

        int pos = 0;
        for (int c = 0; c < words.size(); c++) {
            for (int idx = 0; idx < words.size(); idx++)
            {
                char chars[] = words.get(idx).toCharArray();
                if (chars.length > pos)
                {
                    int startState = 0;
                    for (int i = 0; i <= pos-1; i++)
                    {
                        System.out.println(chars[i]);
                        startState = transfer(startState, getCode(chars[i]));
                    }
                    TrieNode node = insert(startState, getCode(chars[pos]), (chars.length == pos+1), idx);
                    node.setLabel(chars[pos]);
                }
            }
            pos++;
        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 18:54
     *  @Description: 查询匹配项
     */
    public List<Integer> match(String keyWord) {
        List<Integer> result = new ArrayList<Integer>();
        int startState, endState;

        char chars[] = keyWord.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            startState = 0;
            for (int j = i; j < chars.length; j++) {
                endState = transfer(startState, getCode(chars[j]));
                if (base[endState].getTransferRatio() != BASE_NULL && check[endState] == startState) { //节点存在于 Trie 树上
                    if (base[endState].isLeaf()) {
                        if (!result.contains(base[endState].getValue())) {
                            result.add(base[endState].getValue());
                        }
                    }
                    startState = endState;
                } else {
                    break;
                }
            }
        }

        return result;
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 19:00
     *  @Description: 打印DATrie
     */
    public void printTrie() {
        System.out.println();
        System.out.printf("%5s", "idx");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7d\t", i);
            }
        }
        System.out.println();
        System.out.printf("%5s", "base");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7d\t", base[i].getTransferRatio());
            }
        }
        System.out.println();
        System.out.printf("%5s", "leaf");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7d\t", base[i].isLeaf() ? 1 : 0);
            }
        }
        System.out.println();
        System.out.printf("%5s", "idx");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7d\t", base[i].getValue());
            }
        }
        System.out.println();
        System.out.printf("%5s", "char");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7c\t", base[i].getLabel());
            }
        }
        System.out.println();
        System.out.printf("%5s", "check");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7d\t", check[i]);
            }
        }
        System.out.println();
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 18:49
     *  @Description: 根据起始状态和转移技术插入新节点并返回插入的节点
     *  @param startState 起始状态
     *  @param offset  状态偏移量
     *  @param isLeaf  是否为叶子节点
     *  @param idx 当前节点在词典中的索引号
     */
    private TrieNode insert(int startState, int offset, boolean isLeaf, int idx) {
        int endState = transfer(startState, offset); //状态转移

        if (base[endState].getTransferRatio() != BASE_NULL && check[endState] != startState) { //已被占用
            do {
                endState += 1;
            } while (base[endState].getTransferRatio() != BASE_NULL);

            base[startState].setTransferRatio(endState - offset); //改变父节点转移基数(这里有问题)
        }

        if (isLeaf) {
            base[endState].setTransferRatio(Math.abs(base[startState].getTransferRatio())*-1); //叶子节点转移基数标识为父节点转移基数的相反数
            base[endState].setLeaf(true);
            base[endState].setValue(idx); //为叶子节点时需要记录下该词在字典中的索引号
        } else {
            if (base[endState].getTransferRatio() == BASE_NULL) { //未有节点经过
                base[endState].setTransferRatio(Math.abs(base[startState].getTransferRatio())); //非叶子节点的转移基数一定为正
            }
        }
        check[endState] = startState;//check中记录当前状态的父状态

        return base[endState];
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 18:53
     *  @Description: 根据起始状态和转移基数返回结束状态
     */
    private int transfer(int startState, int offset) {
        return Math.abs(base[startState].getTransferRatio())+offset; //状态转移
    }


    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 19:13
     *  @Description: 获取base数组的下标
     */
    private int getCode(char c) {
        return (int)c;//这里必须大于0
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 16:06
     *  @Description: 初始化DATrie（base，check数组全部初始化）
     */
    private void init() {
        base = new TrieNode[ARRAY_SIZE];
        check = new int[ARRAY_SIZE];

        for (int i = 0; i < ARRAY_SIZE; i++) {
            TrieNode node = new TrieNode();
            node.setTransferRatio(BASE_NULL);
            base[i] = node;
            check[i] = CHECK_NULL;
        }

        TrieNode root = new TrieNode();
        root.setTransferRatio(BASE_ROOT);
        base[0] = root;
        check[0] = CHECK_ROOT;
    }


    public static void main(String[] args) {

        List<String> words = new ArrayList<String>();
        words.add("清华");
        /*words.add("清华大学");
        words.add("清新");
        words.add("中华");
        words.add("中华人民");
        words.add("华人");
        words.add("学生");
        words.add("大学生");
        words.add("qin");
        words.add("shi");
        words.add("ming");
        words.add("yue");
        words.add("zhi");
        words.add("jun");
        words.add("lin");
        words.add("tian");
        words.add("xia");
        words.add("中国");
        words.add("人名");
        words.add("中国人民");
        words.add("人民");
        words.add("孙健");
        words.add("CSDN");
        words.add("java");
        words.add("java学习");

        //制作码表，以备验证
        Set<Character> codes = new HashSet<Character>();
        for (String word : words) {
            char chars[] = word.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                codes.add(chars[i]);
            }
        }
        for (Character character : codes) {
            System.out.printf("%6s\t", character.charValue());
        }
        System.out.println();
        for (Character character : codes) {
            System.out.printf("%6d\t",  (int)character.charValue());
        }
        System.out.println();

        //构建 Trie 树
        DATrie daTrie = new DATrie();
        daTrie.build(words);
        daTrie.printTrie();

        //执行匹配
//        List<Integer> result = daTrie.match("清华大学生都是华人");
//        List<Integer> result = daTrie.match("中国人名识别是中国人民的一个骄傲.孙健人民在CSDN中学到了很多最早iteye是java学习笔记叫javaeye但是java123只是一部分");
        List<Integer> result = daTrie.match("qinshimingyuezhijunlintianxia");


        //打印匹配结果
        System.out.println();
        System.out.printf("Match: {");
        for (int i = 0; i < result.size(); i++) {
            if (i == 0) {
                System.out.printf("%s", words.get(result.get(i)));
            } else {
                System.out.printf(", %s", words.get(result.get(i)));
            }
        }
        System.out.printf("}");
        System.out.println();*/

        DATrie daTrie = new DATrie();
        daTrie.build(words);
        daTrie.printTrie();

        List<Integer> result = daTrie.match("清华");
        for (int i = 0; i < result.size(); i++) {
            if (i == 0) {
                System.out.printf("%s", words.get(result.get(i)));
            } else {
                System.out.printf(", %s", words.get(result.get(i)));
            }
        }


    }




}