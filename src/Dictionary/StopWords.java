package Dictionary;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.seg.common.Term;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class StopWords {

    public static String stopWordsPath = "D:\\stopwords.txt";


    /**
     *  @author: Ragty
     *  @Date: 2020/4/11 12:37
     *  @Description: 加载字典到DATrie
     */
    public static DATrie loadStopword(String path) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(stopWordsPath));
        List<String> stopWords = new ArrayList<String>();
        String temp = null;

        while ((temp = bufferedReader.readLine()) != null) {
            stopWords.add(temp.trim());
        }

        DATrie daTrie = new DATrie();
        daTrie.build(stopWords);
        return daTrie;
    }




    /**
     *  @author: Ragty
     *  @Date: 2020/4/11 12:41
     *  @Description: HanLP分词
     */
    private static List<Term> segment(String text) {
        List<Term> list = HanLP.segment(text);
        return list;
    }




    /**
     *  @author: Ragty
     *  @Date: 2020/4/11 13:17
     *  @Description: 停用词过滤
     */
    public static List<Term> removeStopWords(String text, DATrie daTrie) {

        List<Term> list = segment(text);
        ListIterator<Term> listIterator = list.listIterator();

        while(listIterator.hasNext()) {
            if (daTrie.containsKey(listIterator.next().word)) {
                listIterator.remove();
            }
        }

        return list;
    }




    public static void main(String[] args) throws IOException {

        String text = "原来的路已经看不到了，只剩远方模糊的身影";
        DATrie daTrie = loadStopword(text);

        System.out.println("源文本："+ text);
        System.out.println("分词结果："+ segment(text));
        System.out.println("停用词过滤：" + removeStopWords(text,daTrie));

    }




}
