
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import jdk.jshell.execution.Util;
import org.bson.Document;
import org.bson.conversions.Bson;


import javax.print.Doc;
import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.lang.Math.log10;
import static java.util.Arrays.asList;
import static com.mongodb.client.model.Projections.*;


public class Indexer {
    private static long pagesCount = 0;

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

        // TODO: @ lucio
        // TODO: Send these pages to another class in constructor
        Ranker ranker = new Ranker(pages, pagesCount);


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

//        calcRank(calcRelevance());

        // listen for any coming crawled url in order ot index it in the db
//        processNewPage("https://en.wikipedia.org/wiki/Ice_hockey_in_Bosnia_and_Herzegovina");

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
        for(Document doc : docs) {
            double idf = doc.getDouble("idf");
            ArrayList<Document> pages = (ArrayList<Document>) doc.get("pages");
            for(Document pg : pages) {
                double advTf = pg.getDouble("advTermFreq");
                double score = advTf * idf;

                Bson filter = Filters.eq( "_id", doc.get("_id"));
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

//    private static HashMap<String, Double> calcRelevance() {
//
//        Vector<String> words = new Vector<>();
//        words.add("histori");
//        words.add("theme");
//
//        MongoCollection collection = Utility.dbConnect(Utility.WORDS_COLLECTION);
//
//        HashMap<String, Double> pagesRelevance = new HashMap<>();
//        for (String word : words) {
//            Document doc = (Document) collection.find(eq("word", word)).first();
//            ArrayList<Document> pages = (ArrayList<Document>) doc.get("pages");
//            for (Document pg : pages) {
//                String url = pg.getString("url");
//                double score = pg.getDouble("score");
//                if (pagesRelevance.containsKey(url)) {
//                    pagesRelevance.replace(url, pagesRelevance.get(url) + score);
//                } else {
//                    pagesRelevance.put(url, score);
//                }
//            }
//        }
//        // Now, every url has an associated relevance value
//
//        Utility.dbDisconnect();
//
//        for (Map.Entry<String, Double> entry : pagesRelevance.entrySet()) {
//            System.out.println(entry.getKey() + "\t" + entry.getValue());
//        }
//        return pagesRelevance;
//
//
//    }

//    private static void calcRank(HashMap<String, Double> pagesRelevance) {
//
//        // TODO: sort these pages and send a particular number of pages to the Frontend
//        TreeMap<String, Double> treeMap = new TreeMap<>(Comparator.comparingDouble(pagesRelevance::get).reversed());
//        treeMap.putAll(pagesRelevance);
//
//        for (Map.Entry<String, Double> entry : treeMap.entrySet()) {
//            System.out.println(entry.getKey() + "\t" + entry.getValue());
//        }
//
//
//    }


    // Incremental Update: It must be possible to update an existing
    // index with a set of newly crawled HTML documents
    // TODO;
    public static void processNewPage(String url) throws IOException {
        MongoCollection collection = Utility.dbConnect(Utility.WORDS_COLLECTION);

        // TODO: you should update every word's idf in the db because pagesCount has changed due to new crawled pages
//        if (/* newPageCrawled */) {
        PageInfo newPage = new PageInfo(url);

        for (WordInfo word : newPage.getWords()) {
            System.out.println(word.getName());
            dbInsert(collection, word, newPage);
        }
        System.err.println("Number of words: " + newPage.getWords().size());

        // Update every word's IDF
        ++pagesCount;
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


        // TODO: or update nothing in case of existing crawled page changes
//        } else if ( /* anExistingCrawledPageChanges */ ) {
//            PageInfo newPage = new PageInfo(url);
//
//            for (WordInfo word : newPage.getWords()) {
//                System.out.println(word.getName());
//                dbInsert(collection, word, newPage);
//            }
//            System.err.println("Number of words: " + newPage.getWords().size());
//        }

        Utility.dbDisconnect();

    }


}
