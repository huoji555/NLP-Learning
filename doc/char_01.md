## 词典分词

###### 1.概述

`中文分词`指的是将一段文本拆分为一系列单词的过程，这是中文信息处理的第一站，中文分词备受关注。中文分词大致分为以下两类：

- **基于词典规则**
- **基于机器学习**

这里我们主要介绍词典分词



###### 2.词典分词

**词典分词**是最简单，最常见的分词算法，需要的材料为：

- **一部词典**
- **一套查词典的规则**

简单来说，词典分词就是一个确定的查词与输出的规则系统。词典分词的重点不在于分词本身，而在于**支撑词典的数据结构**。



###### 3.词典分类及加载

互联网上有许多公开的中文词典，比如搜狗实验室发布的互联网词库`SogouW`，清华大学开放中文词库`THUOCL`，以及千万级巨型汉语词库`HanLP`。我们这里用`HanLP`



`HanLP`中词典的格式是一种以空格分隔的表格形式，第一列是单词本身，之后表示词性与相应的词频，下面举个栗子：

```java
希望  v  386  n 96
希罕  a  1
希冀  v  1
```



下载的话，需要先下载好`HanLP`的数据包，推荐从`码云`直接[下载](<https://gitee.com/weiyy153/HanLP/tree/1.x/data>)，`GitHub`很慢。

加载的话很简单，我们把下载好的数据包放在`src`目录下，只需要以下的代码即可完成字典的加载：

```java
TreeMap<String,CoreDictionary.Attribute> dictionary = IOUtil.loadDictionary("src/HanLP/data/dictionary/CoreNatureDictionary.mini.txt");
```



###### 4.词典切分算法

词典确定之后，**句子中可能含有很多词典中的词语**。它们可能相互重叠，到底输出哪一个由规定的规则决定。常用的规则有以下几种（都基于完全切分过程）：

- **完全切分算法**
- **正向最长匹配算法**
- **逆向最长匹配算法**
- **双向最长匹配算法**



- **完全切分算法**

  `核心思想`：找出一段文本中的所有单词。

  `代码实现`：

  ```java
  /**
   *  @author: Ragty
   *  @Date: 2020/2/9 21:38
   *  @Description: 完全切分式的中文算法
   */
  public static List<String> segmentFully(String text, TreeMap<String, CoreDictionary.Attribute> dictionary) {
      List<String> wordList = new LinkedList<String>();
      for (int i=0; i< text.length(); i++)
      {
          for(int j=i+1; j<=text.length(); j++)
          {
              String word = text.substring(i,j);
              if (dictionary.containsKey(word))
              {
                  wordList.add(word);
              }
          }
      }
  
      return wordList;
  }
  ```

  `单元测试`：

  ```java
  System.out.println(segmentFully("就读北京大学", dictionary));
  ```

  `测试结果`：

  ```
  [就, 就读, 读, 北, 北京, 北京大学, 京, 大, 大学, 学]
  ```

  `结果分析`:

  以上的输出并不是中文分词，我们需要的是有意义的词语序列，而不是所有出现在字典中的单词所构成的列表，我们需要完善这个规则。



- **正向最长匹配算法**

  `核心思想`：考虑到越长的单词表达的意义越丰富，于是是我们定义单词越长优先级越高。就是在全切分的基础上添加**优先输出更长的单词且扫描顺序为从前往后**的规则。

  `代码实现`：​	

  ```java
  /**
   *  @author: Ragty
   *  @Date: 2020/2/9 22:11
   *  @Description: 正向最长匹配中文分词算法
   */
  public static List<String> segmentForwardLongest (String text, TreeMap<String, CoreDictionary.Attribute> dictionary) {
      List<String> wordList = new LinkedList<String>();
      for (int i=0; i< text.length(); )
      {
          String longestWord = text.substring(i,i+1);
          for(int j=i+1; j<=text.length(); j++)
          {
              String word = text.substring(i,j);
              if (dictionary.containsKey(word))
              {
                  if (word.length() > longestWord.length())
                  {
                      longestWord = word;
                  }
              }
          }
          wordList.add(longestWord);
          i += longestWord.length();
      }
      return wordList;
  }
  ```

  `单元测试`：

  ```java
  System.out.println(segmentForwardLongest("就读北京大学",dictionary));
  System.out.println(segmentForwardLongest("研究生命起源",dictionary));
  ```

  `测试结果`：

  ```
  [就读, 北京大学]
  [研究生, 命, 起源]
  ```

  `结果分析`：

  前一个结果更加符合预期了，但是在第二个实例中，研究生的优先级是大于研究的。我们需要解决这个优先级的冲突。

  

- **逆向最长匹配**

  `核心思想`：与正向匹配的唯一区别在于扫描的方向

  `代码实现`：

  ```java
  /**
   *  @author: Ragty
   *  @Date:  22:25
   *  @Description: 逆向最长匹配的中文分词算法
   */
  public static List<String> segmentBackwardLongest (String text, TreeMap<String, CoreDictionary.Attribute> dictionary) {
      List<String> wordList = new LinkedList<String>();
      for (int i=text.length()-1 ; i >= 0; )
      {
          String longestWord = text.substring(i,i+1);
          for(int j=0; j<=i; j++)
          {
              String word = text.substring(j,i+1);
              if (dictionary.containsKey(word))
              {
                  if (word.length() > longestWord.length())
                  {
                      longestWord = word;
                  }
              }
          }
          wordList.add(0,longestWord);
          i -= longestWord.length();
      }
      return wordList;
  }
  ```

  `单元测试`：

  ```java
  System.out.println(segmentBackwardLongest("研究生命起源",dictionary));
  System.out.println(segmentBackwardLongest("项目的研究",dictionary));
  ```

  `测试结果`：

  ```java
  [研究, 生命, 起源]
  [项，目的，研究]
  ```

  `结果分析`：

  虽然逆向最长匹配算法能解决一些问题，但同时也出现了新的问题。

  

- **双向最长匹配**

  `核心思想`：综合前两种规则，取长补短。规则如下所述

  - 同时执行正向和逆向最长匹配，若两者词数不同，则返回次数更少的一个
  - 否则，返回两者中单字最少的那一个。单子数目相同时，优先返回逆向最长匹配的结果

  `代码实现`：

  ```java
  /**
   *  @author: Ragty
   *  @Date: 2020/2/9 22:29
   *  @Description: 统计分词中的单字数量
   */
  public static int countingSingleChar(List<String> wordList)
  {
      int size = 0;
      for (String word : wordList)
      {
          if (word.length() == 1)
              size++;
      }
      return size;
  }
  
  
  
  /**
   *  @author: Ragty
   *  @Date: 2020/2/9 22:37
   *  @Description: 双向最长匹配的中文分词算法
   */
  public static List<String> segmentBidirectional (String text, TreeMap<String, CoreDictionary.Attribute> dictionary)
  {
      List<String> forwardLongest = segmentForwardLongest(text, dictionary);
      List<String> backwardLongest = segmentBackwardLongest(text, dictionary);
  
      if (forwardLongest.size() < backwardLongest.size())
          return forwardLongest;
      else if (forwardLongest.size() > backwardLongest.size())
          return backwardLongest;
      else
      {
          if (countingSingleChar(forwardLongest) < countingSingleChar(backwardLongest))
              return forwardLongest;
          else
              return backwardLongest;
      }
  }
  ```

  `单元测试`：

  ```java
  System.out.println(segmentBidirectional("研究生命起源",dictionary));
  ```

  `测试结果`：

  ```
  [研究, 生命, 起源]
  ```

  `结果分析`：

  调用了两次匹配算法，所以速度上较慢，而且很多时候是帮倒忙，不实用



**匹配规则效果对比**：

| 序号 | 原文         | 正向最长匹配         | 逆向最长匹配           | 双向最长匹配           |
| ---- | ------------ | -------------------- | ---------------------- | ---------------------- |
| 1    | 项目的研究   | **[项目，的，研究]** | [项，目的，研究]       | [项，目的，研究]       |
| 2    | 商品和服务   | [商品，和服，务]     | **[商品，和，服务]**   | **[商品，和，服务]**   |
| 3    | 研究生命起源 | [研究生，命, 起源]   | **[研究, 生命, 起源]** | **[研究, 生命, 起源]** |



**词典切分算法总结**：

由以上的表格分析，规则系统的脆弱可见一斑。规则集的维护又是是拆东墙补西墙，有时是帮倒忙。



###### 5.字典树

匹配算法的瓶颈之一在于**如何判断字典中是否含有字符串**，如果用的是有序集合(`TreeMap`)的话，复杂度是`O(logn)`，如果用散列表(`HashMap`)，账面上的时间复杂度虽然下降了，但内存复杂度上去了。我们要寻找一种速度又快，又省内存的数据结构。



**字典树概念**：

> 又称单词查找树，[Trie树](https://baike.baidu.com/item/Trie%E6%A0%91)，是一种[树形结构](https://baike.baidu.com/item/%E6%A0%91%E5%BD%A2%E7%BB%93%E6%9E%84/9663807)，是一种哈希树的变种。典型应用是用于统计，排序和保存大量的[字符](https://baike.baidu.com/item/%E5%AD%97%E7%AC%A6)串（但不仅限于字符串），所以经常被搜索引擎系统用于文本词频统计。（看图马上理解）

![](https://img.51wendang.com/pic/1ca3c74ffc3ae1732176b761/2-810-jpg_6-1080-0-0-1080.jpg)



**字典树特点**：

- 根节点不包含字符，除根节点外每一个节点都只包含一个字符
- 从根节点到某一节点，路径上经过的字符连接起来，为该节点对应的字符串
- 每个节点的所有子节点包含的字符都不相同



**字典树的实现原理**：

从确定有限状态自动机(DFA)的角度来讲，每个节点都是一个状态，状态表示当前已经查询到的前缀。从父节点到子节点的移动过程可以看作一次`状态转移`。以下是查询步骤：

- 我们输入一个想要查询的词，如果有满足条件的边，状态转移；如果找不到，直接失败
- 完成了全部转移时，拿到了最后一个字符的状态，询问该状态是否为**终点状态**，如果是则查到了单词，否则该单词不在字典中

"删改改查"都是一回事，以下不再赘述



**字典树节点结构**：

我们为了方便直接用数组实现

```java
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
```



**字典树的实现**：

```java
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
```



**测试**：

```java
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
```



**测试结果**：

```java
true
false
s--a--d--y--t--o--o--
```



**算法分析**：

当字典大小为n时，虽然最坏情怀下字典树的复杂度依然是O(logn)。但它的实际速度比二分查找快，这是因为随着路径的深入，前缀匹配是递进的过程，算法不必比较字符串的前缀，因此可以节省很多用来比较的时间。



**算法改进**：

这里我们查询某个词的时候还需要逐个对比，若我们将对象转换为散列值，散列函数输出区间为[0,65535]之间的整数，这时候我们直接访问下标就可以访问到对应的字符，不过这种做法只适用于第一行，否则会内存指数膨胀，后边的按数组存放即可，查询时直接二分法查询。