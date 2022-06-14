
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
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
    private static boolean firstTime = false;    // false => for incoming pages

    public static void main(String[] args) throws IOException, InterruptedException {


        Vector<PageInfo> pages = new Vector<PageInfo>();
        Vector<String> urls = Utility.readFile(Utility.URLS_FILE_PATH);

        pagesCount = urls.size();

        // Setup every crawled page
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

        // if there is a new crawled page
        if (!firstTime) {
            processNewPage("https://www.javatpoint.com/aws-tutorial", pages);
            return;
        }

        new Ranker(pages, pagesCount);


        MongoCollection collection = Utility.dbConnect(Utility.WORDS_COLLECTION);
        collection.drop();

        // Insert every word in each page in the DB
        for (PageInfo page : pages) {
            for (WordInfo word : page.getWords()) {
                System.out.println(word.getName());
                dbInsert(collection, word, page);
            }
            System.err.println("Number of words: " + page.getWords().size());
        }

        insertScores(collection);

        Utility.dbDisconnect();


    }

    private static Document createPageDoc(WordInfo word, PageInfo page, double idf) {
        return new Document()
                .append("url", page.getUrl())
                .append("title", page.getTitle())
                .append("termFreq", page.getNormTermFreq(word.getName()))
                .append("advTermFreq", page.getNormAdvTermFreq(word.getName()))
                .append("score", idf * page.getNormAdvTermFreq(word.getName()))
                .append("tags", word.getTags())
                .append("weights", word.getWeights())
                .append("sentences", word.getSentences());
    }

    private static void dbInsert(MongoCollection collection, WordInfo word, PageInfo page) {

        Document wordDoc = (Document) collection.find(eq("word", word.getName())).first();

        // If the word doesn't exist, add it
        if (wordDoc == null) {
            Document document = new Document()
                    .append("word", word.getName())
                    .append("df", (long) 1)
                    .append("idf", log10((double) pagesCount / 1))
                    .append("pages", asList(createPageDoc(word, page, (double) pagesCount / 1)));
            collection.insertOne(document);

            System.err.println("NEW");
        } else { // you are in a new page, because every pageInfo has unique words
            long docFreq = (long) wordDoc.get("df");
            ++docFreq;
            double newIdf = log10((double) pagesCount / docFreq);

            Bson update = Updates.combine(
                    Updates.addToSet("pages", createPageDoc(word, page, newIdf)),
                    Updates.set("df", docFreq),
                    Updates.set("idf", newIdf)
            );

            collection.updateOne(wordDoc, update);
            System.err.println("EXISTS");

        }
    }

    private static void insertScores(MongoCollection collection) {

        List<Document> docs = (List<Document>) collection.find().into(new ArrayList<Document>());
        for (Document doc : docs) {
            double idf = doc.getDouble("idf");
            ArrayList<Document> pages = (ArrayList<Document>) doc.get("pages");
            for (Document pg : pages) {
                double advTf = pg.getDouble("advTermFreq");
                double score = advTf * idf;

                Bson filter = Filters.eq("_id", doc.get("_id"));
                Bson pulling = Updates.pull("pages", new Document("url", pg.getString("url")));
                collection.updateOne(filter, pulling);

                Bson adding = Updates.push("pages", new Document()
                        .append("url", pg.getString("url"))
                        .append("title", pg.getString("title"))
                        .append("termFreq", pg.getDouble("termFreq"))
                        .append("advTermFreq", pg.getDouble("advTermFreq"))
                        .append("score", score)
                        .append("tags", (List<String>) pg.get("tags"))
                        .append("weights", (List<String>) pg.get("weights"))
                        .append("sentences", (List<String>) pg.get("sentences"))
                );

                collection.updateOne(filter, adding);
            }
        }
    }


    public static void processNewPage(String url, Vector<PageInfo> pages) throws IOException {
        MongoCollection collection = Utility.dbConnect(Utility.WORDS_COLLECTION);

        // update every word's idf in the db because pagesCount has changed due to new crawled pages
        PageInfo newPage = new PageInfo(url);
        ++pagesCount;

        // Drop and recreate pages' popularity
        pages.add(newPage);
        new Ranker(pages, pagesCount);

        // Insert the new page to the db
        for (WordInfo word : newPage.getWords()) {
            System.out.println(word.getName());
            dbInsert(collection, word, newPage);
        }
        System.err.println("Number of words: " + newPage.getWords().size());

        // Update every word's IDF
        MongoCursor<Document> docCursor = collection.find().cursor();

        try {
            while (docCursor.hasNext()) {
                Document wordDoc = docCursor.next();
                long docFreq = (long) wordDoc.get("df");
                Bson update = Updates.set("idf", log10((double) pagesCount / docFreq));
                collection.updateOne(wordDoc, update);
            }
        } finally {
            docCursor.close();
        }

        // Then update the score of each page using the new idf
        insertScores(collection);

        Utility.dbDisconnect();

    }


}
