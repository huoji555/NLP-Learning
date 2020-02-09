package Dictionary;

import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.hanlp.dictionary.CoreDictionary;

import java.io.IOException;
import java.util.*;

public class Segment {



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



    public static void main(String[] args) throws IOException {

        TreeMap<String,CoreDictionary.Attribute> dictionary = IOUtil.loadDictionary("src/HanLP/data/dictionary/CoreNatureDictionary.mini.txt");
        System.out.println(segmentFully("就读北京大学", dictionary));

        System.out.println(segmentForwardLongest("就读北京大学",dictionary));
        System.out.println(segmentForwardLongest("研究生命起源",dictionary));

        System.out.println(segmentBackwardLongest("研究生命起源",dictionary));

        System.out.println(segmentBidirectional("研究生命起源",dictionary));
    }


}
