

import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

public class Indexer {

    public static void main(String[] args) throws IOException {
        Vector<PageInfo> pages = new Vector<PageInfo>();
        Vector<String> urls = Utility.readFile(Utility.URLS_FILE_PATH);

        for (String url : urls) {
            pages.add(new PageInfo(url));
        }


        Vector<PageInfo> pgs = new Vector<PageInfo>();
        HashSet<String> s = new HashSet<>();
        for (PageInfo page : pages) {
            for (WordInfo word : page.getWords()) {
//                insertIntoDB(word, page)
            }
        }


//        for (PageInfo p : pgs) {
//            System.out.printf("URL: %s\n", p.getUrl());
//            System.out.printf("Title: %s\n", p.getTitle());
//            System.out.printf("TF: %d\n", p.getTermFreq("time"));
//            System.out.printf("NTF: %f\n", p.getNormTermFreq("time"));
//            System.out.println();
//        }
    }

    private static void insertIntoDB(WordInfo word, PageInfo page) {

    }


}
