import com.mongodb.client.MongoCollection;
import jdk.jshell.execution.Util;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.jar.JarOutputStream;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.Math.log10;
import static java.util.Arrays.asList;

public class Ranker {
    static int MAXITERATION = 1;

    private Vector<PageInfo> pages;
    private long numberOfPages;

    public Ranker(Vector<PageInfo> pgs, long pagesCount) throws IOException {
        this.pages = pgs;
        this.numberOfPages = pagesCount;

        setPopularityRank();
    }


    private void SetPageRank() throws IOException {
        double score;

        for (int i = 0; i < this.numberOfPages; i++) {
            score = 0.0;
            for (int j = 0; j < this.numberOfPages; j++) {
                if (!this.pages.get(i).getUrl().equals(this.pages.get(j).getUrl()) && this.pages.get(j).getLinks().contains(this.pages.get(i).getUrl())) {
                    System.out.println("Matched: " + this.pages.get(i).getUrl());

                    score += (this.pages.get(j).getPreviousPopularityScore() / this.pages.get(j).getLinks().size());


                    System.err.println("Matched link | score: " + score);
                } else {
                    System.err.println("NOT Matched link | score: " + score);
                }
            }
            this.pages.get(i).setCurrentPopularityScore(score);
        }


        for (int i = 0; i < this.numberOfPages; i++) {
            this.pages.get(i).setPreviousPopularityScore(this.pages.get(i).getCurrentPopularityScore());
        }
    }

    private void setPopularityRank() throws IOException {
        double firstScore = 1.0 / this.numberOfPages;
        for (int i = 0; i < this.numberOfPages; i++) {
            this.pages.get(i).setPreviousPopularityScore(firstScore);
            this.pages.get(i).setCurrentPopularityScore(firstScore);
        }

        for (int i = 0; i < MAXITERATION; i++) {
            SetPageRank();
        }

        // Insert in the db

        MongoCollection collection = Utility.dbConnect(Utility.PAGES_COLLECTION);
        collection.drop();

        for (PageInfo page : this.pages) {
            Document document = new Document()
                    .append("url", page.getUrl())
                    .append("popularity", page.getCurrentPopularityScore());

            collection.insertOne(document);
        }

        Utility.dbDisconnect();
    }

    public static HashMap<String, Double> calcRelevance(Vector<String> words) {

        MongoCollection collection = Utility.dbConnect(Utility.WORDS_COLLECTION);

        HashMap<String, Double> pagesRelevance = new HashMap<>();
        for (String word : words) {
            Document doc = (Document) collection.find(eq("word", word)).first();
            ArrayList<Document> pages = (ArrayList<Document>) doc.get("pages");
            for (Document pg : pages) {
                String url = pg.getString("url");
                double score = pg.getDouble("score");

                if (pagesRelevance.containsKey(url)) {
                    pagesRelevance.replace(url, pagesRelevance.get(url) + score);
                } else {
                    pagesRelevance.put(url, score);
                }
            }
        }
        // Now, every url has an associated relevance value

        Utility.dbDisconnect();

//        System.out.println("Relevances");
//        for (Map.Entry<String, Double> entry : pagesRelevance.entrySet()) {
//            System.out.println(entry.getKey() + "\t" + entry.getValue());
//        }
        return pagesRelevance;
    }

    public static List<Map.Entry<String, Double>> calcRank(HashMap<String, Double> pagesRelevance) {

        MongoCollection collection = Utility.dbConnect(Utility.PAGES_COLLECTION);

        for (Map.Entry<String, Double> entry : pagesRelevance.entrySet()) {

            Document doc = (Document) collection.find(eq("url", entry.getKey())).first();
            double popularity = doc.getDouble("popularity");
            entry.setValue(popularity * entry.getValue());
        }

        Utility.dbDisconnect();

        // sort these pages and send a particular number of pages to the Frontend
//        TreeMap<String, Double> treeMap = new TreeMap<>(Comparator.comparingDouble(pagesRelevance::get).reversed());
        List<Map.Entry<String, Double>> pagesList = new LinkedList<>(pagesRelevance.entrySet());
        Collections.sort(pagesList, new Comparator<>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        return pagesList;


//        System.out.println("Total value");
//        for (Map.Entry<String, Double> entry : list) {
//            System.out.println(entry.getKey() + "\t" + entry.getValue());
//        }

    }

}
