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
    public static List<String> segmentForwordLongest (String text, TreeMap<String, CoreDictionary.Attribute> dictionary) {
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





    public static void main(String[] args) throws IOException {

        TreeMap<String,CoreDictionary.Attribute> dictionary = IOUtil.loadDictionary("src/HanLP/data/dictionary/CoreNatureDictionary.mini.txt");
        System.out.println(segmentFully("就读北京大学", dictionary));
        System.out.println(segmentForwordLongest("就读北京大学",dictionary));
        System.out.println(segmentForwordLongest("研究生命起源",dictionary));
    }


}
