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

这里我们用`HashMap`实现

```java
/**
* @Author Ragty
* @Description  字典树节点
* @Date   2020/2/27 0:00
*/
class TrieNode {
    public int path;        //表示多少个词共用该前缀
    public boolean status;
    public HashMap<Character, TrieNode> map;

    public TrieNode() {
        path = 0;
        status = false;
        map = new HashMap<>();
    }
}
```



**字典树的实现**：

```java
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
 *  @Description: 前缀遍历，若有前缀，返回它最后一个节点的path
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


/**
 *  @author: Ragty
 *  @Date: 2020/2/27 0:50
 *  @Description: 前序遍历
 */
public void preWalk(TrieNode root) {
    TrieNode node = root;
    for (Map.Entry<Character,TrieNode> map : root.map.entrySet()) {
        node = map.getValue();
        if (node != null) {
            System.out.println(map.getKey());
            preWalk(node);
        }
    }
}


public TrieNode getRoot() {
    return root;
}

```



**测试**：

```java
public static void main(String[] args) {
    TrieTree1 trieTree = new TrieTree1();

    trieTree.insert("字典树");
    trieTree.insert("字典书");
    trieTree.insert("字典");
    trieTree.insert("天气");
    trieTree.insert("气人");

    System.out.println(trieTree.search("字典"));
    System.out.println(trieTree.search("字"));
    System.out.println(trieTree.prefixNumber("字典树"));

    TrieNode root = trieTree.getRoot();

    trieTree.preWalk(root);

}
```



**测试结果**：

```java
true
false
气--人--字--典--树--书--天--气
```



**算法分析**：

当字典大小为n时，虽然最坏情怀下字典树的复杂度依然是O(logn)。但它的实际速度比二分查找快，这是因为随着路径的深入，前缀匹配是递进的过程，算法不必比较字符串的前缀，因此可以节省很多用来比较的时间。



**算法改进**：

这里我们查询某个词的时候还需要逐个对比，若我们将对象转换为散列值，散列函数输出区间为[0,65535]之间的整数，这时候我们直接访问下标就可以访问到对应的字符，不过这种做法只适用于第一行，否则会内存指数膨胀，后边的按数组存放即可，查询时直接二分法查询。



###### 6.双数组字典树

**背景**

`Trie树`本质是一个确定的有限状态自动机(DFA)，核心思想是空间换时间，利用字符串的公共前缀来降低查询时间的开销以达到提高效率的目的。**但由于`Trie树`的稀疏现象严重，空间利用率较低。**为了让`Trie树`实现占用较少的空间，同时还要保证查询的效率，最后提出了用2个线性数组来进行`Trie树`的表示，即`双数组Trie(Double Array Trie)`.



**算法及公式解析**

```java
base[s] + c = t
check[t] =  s
```

往往读到这里，大家都是一头雾水，不知所云，我们首先了解下`base`和`check`代表的意义及作用

> base数组的每个元素表示一个`Trie节点`，即一个状态(分为空闲状态和占用状态)
> check数组的每个元素表示某个状态的前驱状态

现在我们分析一下以上出现的公式

>base树组中的`s`代表当前状态的下标，`t`代表转移状态的下标，`c`代表输入字符的数值
>
>base[s] + c =  t   //表示一次状态转移
>
>由于转移后状态下标为`t`,且父子关系是唯一的，所以可通过检验当前元素的前驱状态确定转移是否成功
>
>check[t] =  s    //检验状态转移是否成功

那么这种算法相对于传统的`Trie树`的**优点**是，**只需要一个加法一次比较即可完成一次状态转移，只花费了常数时间**，下面给出了`双数组Trie树`的原理图（注意观察状态转移的过程）

![](https://img-blog.csdnimg.cn/20200310233002360.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2h1b2ppNTU1,size_16,color_FFFFFF,t_70)



**状态冲突及解决方案(划重点)**

> 说的简单一点，状态冲突的意思就是，进行状态转移时，发现转换的位置base[t]已经被人占了(**状态冲突**)，那你怎么办呢，重新改变c值(**改变父节点的转移基数**)，让它放在base数组中未被占用的位置
>
> 解决方案，用while函数由发生冲突的位置向前遍历，一旦发现有空位置便占用并更新转移基数(**也就是c值**)，可以把这个过程看作为公交车上从后往前占座的过程
>
> 构造字典时，如果有新词加入，若新词的首字未出现，写入时有冲突的情况下，导致根节点的转移基数改变，会导致重构整个树的情况(否则不能进行正确的状态转移)，所以**构建树时建议先构建每个词的首字，再构建各个词的子节点**，这样产生冲突的情况下，可以将冲突局限在单个父节点和子节点之间，不至于大范围的节点重构



**叶子节点的构造与处理**

下面介绍几种处理叶子节点的处理方案：

- **将每个词的词尾设置为特殊字符(/0)**，因为最后一个字已经不需要状态转移，所以可以这样构造，但是增加了节点的数量，构建字典时会造成消耗
- **将每个词的词尾设置为转移基数的负数(只有词尾为负值)**，这样能够节省构建时间，不过进行转移时要将状态转移函数改为`|base[s]|+code(字符)`

我们的实现中采用后一种构建方案



**双字典树结构**

```java
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
```



**双字典树的构建**

> 这里说一句，构建的准则是，**先构建每个词的首字，后构建每个词的剩余节点**

```java
/**
 *  @author: Ragty
 *  @Date: 2020/3/10 19:37
 *  @Description: 构造DATrie
 */
public void build(List<String> words) {
    init();

    boolean shut = false;
    for (int idx = 0; idx < words.size(); idx++)
    {
        int startState = 0;
        char chars[] = words.get(idx).toCharArray();

        if (shut == false) {
            TrieNode node = insert(startState, getCode(chars[0]), (chars.length == 1), idx);
            node.setLabel(chars[0]);
        } else {
            for (int j=1; j<chars.length; j++) {
                startState = transfer(startState, getCode(chars[j-1]));
                TrieNode node = insert(startState, getCode(chars[j]), (chars.length == j+1), idx);
                node.setLabel(chars[j]);
            }
        }

        if (idx == words.size()-1 && shut == false) {
            idx = -1;   //因为开始的时候还有一个加的过程
            shut = true;
        }

    }
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
```



**双数组字典树的插入**

> 插入时，有冲突需要解决冲突，无冲突再检查是否为叶子节点，最后进行状态转移

```java
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

        base[startState].setTransferRatio(endState - offset); //改变父节点转移基数

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
```



**双数组字典树的查询**

这里我写的比较简单，用**正向匹配**做的，这里比较关键的一句是这个

>`base[endState].getTransferRatio() != BASE_NULL && check[endState] == startState`

可以检测出节点是否在树上

```java
/**
 *  @author: Ragty
 *  @Date: 2020/3/5 18:54
 *  @Description: 查询匹配项(正向匹配)
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
```



**双数组字典树测试**

```java
public static void main(String[] args) {

    List<String> words = new ArrayList<String>();
    words.add("清华");
    words.add("清华大学");
    words.add("清新");
    words.add("中华");
    words.add("中华人民");
    words.add("华人");
    words.add("学生");
    words.add("大学生");
    words.add("wo");
    words.add("shi");
    words.add("human");
    words.add("this");
    words.add("is");
    words.add("ragty");
    words.add("pump");
    words.add("it");
    words.add("up");
    words.add("中国");
    words.add("人名");
    words.add("中国人民");
    words.add("人民");
    words.add("java");
    words.add("java学习");

    //构建 Trie 树
    DATrie daTrie = new DATrie();
    daTrie.build(words);
    daTrie.printTrie();

    String keyWord = "清华大学生都是华人";
    List<Integer> result = daTrie.match(keyWord);
    System.out.println();
    System.out.println("输入语句为："+keyWord);

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
    System.out.println();
    
}
```



**测试结果**

```java
  idx      0	     98	     99	    100	    104	    105	    106	    107	    108	    109	    110	    111	    112	    113	    114	    115	    116	    117	    118	    119	    120	    121	    122	    123	    124	    125	    126	    127	    128	    129	    130	    131	    132	  20014	  20099	  20155	  20156	  20157	  20158	  21327	  21328	  21329	  21518	  22272	  22824	  22825	  23399	  23400	  23401	  23433	  26034	  27666	  27668	  27669	  28166	  29984	  29986	
 char   null	      a	      a	      a	      g	      h	      i	      j	      h	      i	      h	      m	      o	      p	      n	      r	      s	      t	      u	      u	      w	      i	      s	      t	      u	      m	      t	      p	      y	      p	      v	      s	      a	      中	      习	      人	      人	      人	      人	      华	      华	      华	      名	      国	      大	      大	      学	      学	      学	      学	      新	      民	      民	      民	      清	      生	      生	
 base      1	      1	      4	     12	      7	      2	     10	      3	      4	     -4	     16	      2	     -1	      7	     -4	      1	      4	      6	     15	      2	      1	     16	     -7	      7	     16	     17	    -10	    -15	     -7	    -17	     35	    -16	    -35	      3	    -35	      1	     -2	      3	      4	      2	     -2	     -3	     -1	      4	      3	      2	      1	     -2	      3	     35	     -2	     -1	     -3	     -4	      2	     -1	     -3	
check     -1	    115	    111	    107	     98	      0	      0	      0	    116	    108	    117	    119	    120	      0	     99	      0	      0	      0	      0	    105	      0	    110	    106	    104	    113	    124	    106	    118	    123	    125	    100	    121	    130	      0	  23433	      0	  21327	  21329	  22272	      0	  28166	  20014	  20155	  20014	      0	  21328	      0	  22825	  22824	    132	  28166	  20155	  20157	  20158	      0	  23399	  23401	
 leaf      否	      否	      否	      否	      否	      否	      否	      否	      否	      是	      否	      否	      是	      否	      是	      否	      否	      否	      否	      否	      否	      否	      是	      否	      否	      否	      是	      是	      是	      是	      否	      是	      是	      否	      是	      否	      是	      否	      否	      否	      是	      是	      是	      是	      否	      否	      否	      是	      否	      否	      是	      是	      是	      是	      否	      是	      是	
  idx     -1	     -1	     -1	     -1	     -1	     -1	     -1	     -1	     -1	      9	     -1	     -1	      8	     -1	     10	     -1	     -1	     -1	     -1	     -1	     -1	     -1	     12	     -1	     -1	     -1	     15	     16	     13	     14	     -1	     11	     21	     -1	     22	     -1	      5	     -1	     -1	     -1	      0	      3	     18	     17	     -1	     -1	     -1	      1	     -1	     -1	      2	     20	      4	     19	     -1	      6	      7	
[0, 1, 7, 6, 5]

输入语句为：清华大学生都是华人

Match: {清华, 清华大学, 大学生, 学生, 华人}

```



**总结**

> 我刚开始写的时候没有任何头绪，看到那两个公式一头雾水，查了很多博客也写得一知半解，而且没有公式解析和实现代码。经过查阅文献以及自己的思考，有了这篇文章，希望能帮到更多想了解`DATrie`的人。
>
> 任何事情你清楚他要解决的问题和实现原理后，会发现它很简单。就像`DATrie`，它的难点在于核心公式的理解以及对于冲突的解决方案。只要理解了这个，实现是很简单的一件事。



**完整代码(可直接运行)**

```java
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
     *  @Date: 2020/3/10 19:37
     *  @Description: 构造DATrie
     */
    public void build(List<String> words) {
        init();

        boolean shut = false;
        for (int idx = 0; idx < words.size(); idx++)
        {
            int startState = 0;
            char chars[] = words.get(idx).toCharArray();

            if (shut == false) {
                TrieNode node = insert(startState, getCode(chars[0]), (chars.length == 1), idx);
                node.setLabel(chars[0]);
            } else {
                for (int j=1; j<chars.length; j++) {
                    startState = transfer(startState, getCode(chars[j-1]));
                    TrieNode node = insert(startState, getCode(chars[j]), (chars.length == j+1), idx);
                    node.setLabel(chars[j]);
                }
            }

            if (idx == words.size()-1 && shut == false) {
                idx = -1;   //因为开始的时候还有一个加的过程
                shut = true;
            }

        }
    }



    /**
     *  @author: Ragty
     *  @Date: 2020/3/5 18:54
     *  @Description: 查询匹配项(正向匹配)
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
        System.out.printf("%5s", "char");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7c\t", base[i].getLabel());
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
        System.out.printf("%5s", "check");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7d\t", check[i]);
            }
        }
        System.out.println();
        System.out.printf("%5s", "leaf");
        for (int i = 0; i < ARRAY_SIZE; i++) {
            if (base[i].getTransferRatio() != BASE_NULL) {
                System.out.printf("%7s\t", base[i].isLeaf() ? "是" : "否");
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

            base[startState].setTransferRatio(endState - offset); //改变父节点转移基数

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
        words.add("清华大学");
        words.add("清新");
        words.add("中华");
        words.add("中华人民");
        words.add("华人");
        words.add("学生");
        words.add("大学生");
        words.add("wo");
        words.add("shi");
        words.add("human");
        words.add("this");
        words.add("is");
        words.add("ragty");
        words.add("pump");
        words.add("it");
        words.add("up");
        words.add("中国");
        words.add("人名");
        words.add("中国人民");
        words.add("人民");
        words.add("java");
        words.add("java学习");



        //构建 Trie 树
        DATrie daTrie = new DATrie();
        daTrie.build(words);
        daTrie.printTrie();

        String keyWord = "清华大学生都是华人";
        List<Integer> result = daTrie.match(keyWord);
        System.out.println();
        System.out.println("输入语句为："+keyWord);

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
        System.out.println();
    }

}
```

