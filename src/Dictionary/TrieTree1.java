package Dictionary;

import java.util.HashMap;

public class TrieTree1 {

    /**
    * @Author Ragty
    * @Description  字典树节点
    * @Date   2020/2/27 0:00
    */
    class TrieNode {
        public int path;
        public boolean status;
        public HashMap<Character, TrieNode> map;

        public TrieNode() {
            path = 0;
            status = false;
            map = new HashMap<>();
        }
    }



    private TrieNode root;


    /**
     *  @author: Ragty
     *  @Date: 2020/2/27 0:01
     *  @Description: 初始化
     */
    public TrieTree1() {
        root = new TrieNode();
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/2/27 0:02
     *  @Description: 插入节点
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        TrieNode node = root;
        node.path++;
        char[] words = word.toCharArray();
        for (int i = 0; i < words.length; i++) {
            if (node.map.get(words[i]) == null) {
                node.map.put(words[i], new TrieNode());
            }
            node = node.map.get(words[i]);
            node.path++;
        }
        node.status = true;
    }


    /**
     *  @author: Ragty
     *  @Date: 2020/2/27 0:02
     *  @Description: 寻找节点
     */
    public boolean search(String word) {
        if (word == null)
            return false;
        TrieNode node = root;
        char[] words = word.toCharArray();
        for (int i = 0; i < words.length; i++) {
            if (node.map.get(words[i]) == null)
                return false;
            node = node.map.get(words[i]);
        }
        return node.status;
    }


    /**
     *  @author: Ragty
     *  @Date: 2020/2/27 0:06
     *  @Description: 删除节点
     */
    public void delete(String word) {
        if (search(word)) {
            char[] words = word.toCharArray();
            TrieNode node = root;
            node.path--;
            for (int i = 0; i < words.length; i++) {
                if (--node.map.get(words[i]).path == 0) {
                    node.map.remove(words[i]);
                    return;
                }
                node = node.map.get(words[i]);
            }
        }
    }




    /**
     *  @author: Ragty
     *  @Date: 2020/2/27 0:07
     *  @Description: 前缀遍历，查找词，若有返回词的长度，没有返回0
     */
    public int prefixNumber(String pre) {
        if (pre == null)
            return 0;
        TrieNode node = root;
        char[] pres = pre.toCharArray();
        for (int i = 0; i < pres.length; i++) {
            if (node.map.get(pres[i]) == null)
                return 0;
            node = node.map.get(pres[i]);
        }
        return node.path;
    }


    public static void main(String[] args) {
        TrieTree1 trieTree = new TrieTree1();

        trieTree.insert("字典树");
        trieTree.insert("字典");
        trieTree.insert("天气");
        trieTree.insert("气人");

        System.out.println(trieTree.search("字典"));
        System.out.println(trieTree.search("字"));

    }

}
