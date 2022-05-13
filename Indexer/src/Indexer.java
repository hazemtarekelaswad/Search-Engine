
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;


import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.Math.log10;
import static java.util.Arrays.asList;


public class Indexer {
    private static long pagesCount = 0;

    public static void main(String[] args) throws IOException, InterruptedException {

        MongoCollection collection = Utility.dbConnect();

        collection.drop();

        Vector<PageInfo> pages = new Vector<PageInfo>();
        Vector<String> urls = Utility.readFile(Utility.URLS_FILE_PATH);

        pagesCount = urls.size();

        long threadsIndex = 0;
        Thread[] threads = new Thread[(int) pagesCount];
        for (String url : urls) {
            threads[(int) threadsIndex] = new Thread() {
                public void run() {
                    try {
                        pages.add(new PageInfo(url));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            threads[(int) threadsIndex++].start();
        }
        for (Thread thread : threads) thread.join();


        for (PageInfo page : pages) {
            for (WordInfo word : page.getWords()) {
                System.out.println(word.getName());
                dbInsert(collection, word, page);
            }
            System.err.println("Number of words: " + page.getWords().size());
        }

        Utility.dbDisconnect();
    }

    private static Document createPageDoc(WordInfo word, PageInfo page) {
        return new Document()
                .append("url", page.getUrl())
                .append("title", page.getTitle())
                .append("termFreq", page.getNormTermFreq(word.getName()))
                .append("tags", word.getTags())
                .append("sentences", word.getSentences());
    }

    private static void dbInsert(MongoCollection collection, WordInfo word, PageInfo page) {

        Document wordDoc = (Document) collection.find(eq("word", word.getName())).first();

        if (wordDoc == null) {
            Document document = new Document()
                    .append("word", word.getName())
                    .append("df", (long) 1)
                    .append("idf", log10((double) pagesCount / 1))
                    .append("pages", asList(createPageDoc(word, page)));
            collection.insertOne(document);
        } else {
            long docFreq = (long) wordDoc.get("df");
            ++docFreq;
            Bson update = Updates.combine(
                    Updates.addToSet("pages", createPageDoc(word, page)),
                    Updates.set("df", docFreq),
                    Updates.set("idf", log10((double) pagesCount / docFreq))
            );
            collection.updateOne(wordDoc, update);
        }
    }

    // Incremental Update: It must be possible to update an existing
    // index with a set of newly crawled HTML documents

    public static void detectChanges(String url) throws IOException {
        MongoCollection collection = Utility.dbConnect();

        PageInfo newPage = new PageInfo(url);
        ++pagesCount;
        // TODO: you should update every word's idf in the db because pagesCount has changed

        for (WordInfo word : newPage.getWords()) {
            System.out.println(word.getName());
            dbInsert(collection, word, newPage);
        }
        System.err.println("Number of words: " + newPage.getWords().size());

        Utility.dbDisconnect();

    }


}
