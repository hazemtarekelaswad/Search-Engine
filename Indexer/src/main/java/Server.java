import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.*;

import static spark.Spark.*;

public class Server {
    private static final int PORT_NUMBER = 3000;

    private static org.json.JSONObject processSearchQueries(Vector<String> queries) throws IOException {

        // Remove stop words and duplicated words
        Vector<String> stopWords = Utility.readFile(Utility.STOP_WORDS_FILE_PATH);
        HashSet<String> refinedWords = new HashSet<>();
        for (String query : queries) {
            if (!stopWords.contains(query.toLowerCase())) {
                refinedWords.add(query.toLowerCase());
            }
        }

        // Stem every word
        Vector<String> words = new Vector<>();
        for (String word : refinedWords) {
            words.add(Utility.stemWord(word));
        }

        // Send these words to the ranker to calculate factors and return list of ranked pages
        HashMap<String, PageJson> pagesRelevance = Ranker.calcRelevance(words);
        List<Map.Entry<String, PageJson>> resultPages = Ranker.calcRank(pagesRelevance);

        for (Map.Entry<String, PageJson> entry : resultPages) {
            System.out.println(entry.getKey() + "\t" + entry.getValue().score);
        }

        // Prepare the json file to send
        org.json.JSONObject json = new org.json.JSONObject();

        JSONArray jsonArr = new JSONArray();
        for (Map.Entry<String, PageJson> pg : resultPages) {
            org.json.JSONObject page = new org.json.JSONObject();
            page.put("url", pg.getKey());
            page.put("title", pg.getValue().title);
            page.put("paragraph", pg.getValue().paragraph);

            jsonArr.put(page);
        }

        json.put("results", resultPages.size());
        json.put("pages", jsonArr);

        return json;
    }

    public static void main(String[] args) {
        System.out.println("Server is running...");
        port(PORT_NUMBER);
        post("/pages", (req, res) -> {
            JSONParser parser = new JSONParser();
            JSONObject request = (JSONObject) parser.parse(req.body());
            String query = (String) request.get("query");
            String[] queries = query.split(" ");
            Vector<String> words = new Vector<>(Arrays.asList(queries));

            // Processing
            org.json.JSONObject response = processSearchQueries(words);

            res.type("application/json");

            if ((int) response.get("results") == 0) {
                res.status(400);
            } else {
                res.status(200);
            }

            return response;

        });
    }

}
