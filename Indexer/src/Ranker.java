import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Ranker {

    private Vector<PageInfo> Pages = new Vector<PageInfo>();
    static int NUMBER_OF_PAGES = 5000;
    private long CurrentScore;
    private long lastScore;
    private Vector<String> links;
    static int MAXITERATION = 4;

    public Ranker(PageInfo pages) {
//        this.Pages = pages;
    }

    public long getCurrentScore() {
        return this.CurrentScore;
    }

    public void setCurrentScore(long score) {
        this.CurrentScore = score;
    }

    public long getlastScore() {
        return this.lastScore;
    }

    public void setlastScore(long score) {
        this.lastScore = score;
    }

//    private void SetPageRank() {
//        long score;
//
//        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
//            score = 0;
//            for (int j = 0; j < NUMBER_OF_PAGES; j++) {
//                if (Page[j].getLinks().contains(Page[i].getUrl())) {
//                    score += (Page[j].getlastScore() / Page[j].getLinks().size())
//
//                }
//            }
//            Page[i].setCurrentScore(score);
//        }
//
//
//        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
//            Page[i].setlastScore(getCurrentScore());
//        }
//    }
//
//    public void setPopularityRank() {
//        long firstScore = 1 / NUMBER_OF_PAGES;
//        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
//            Page[i].setlastScore(firstScore);
//            page[i].setCurrentScore(firstScore);
//        }
//
//        for (int i = 0; i < MAXITERATION; i++) {
//            SetPageRank();
//        }
//
//    }

    private HashMap<String, Double> calcRelevance(Vector<String> words) {
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

//        for (Map.Entry<String, Double> entry : pagesRelevance.entrySet()) {
//            System.out.println(entry.getKey() + "\t" + entry.getValue());
//        }
        return pagesRelevance;


    }

    private void calcRank(HashMap<String, Double> pagesRelevance) {

        MongoCollection collection = Utility.dbConnect(Utility.PAGES_COLLECTION);

        for (Map.Entry<String, Double> entry : pagesRelevance.entrySet()) {

            Document doc = (Document) collection.find(eq("url", entry.getKey())).first();
            double popularity = doc.getDouble("popularity");
            entry.setValue(popularity * entry.getValue());
        }

        Utility.dbDisconnect();

        // TODO: sort these pages and send a particular number of pages to the Frontend
        TreeMap<String, Double> treeMap = new TreeMap<>(Comparator.comparingDouble(pagesRelevance::get).reversed());
        treeMap.putAll(pagesRelevance);


        // TODO: send these urls to the frontend

    }

    public static void main(String[] args) {
         // HashMap<String, Double> pagesRelevance = calcRelevance(/*words*/);
        // calcRank(pagesRelevance)
    }

}
