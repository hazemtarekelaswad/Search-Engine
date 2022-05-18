import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;

import static spark.Spark.get;
import static spark.Spark.port;

public class Server {
    private static final int PORT_NUMBER = 3000;

    private static org.json.JSONObject processSearchQueries(Vector<String> words) {
        HashMap<String, Double> pagesRelevance = Ranker.calcRelevance(words);
        List<Map.Entry<String, Double>> resultPages = Ranker.calcRank(pagesRelevance);

        System.out.println(resultPages);

        // Prepare the json file to send
        org.json.JSONObject json = new org.json.JSONObject();

        JSONArray jsonArr = new JSONArray();
        for (Map.Entry<String, Double> pg : resultPages) {
            org.json.JSONObject page = new org.json.JSONObject();
            page.put("url", pg.getKey());
            jsonArr.put(page);
        }

        json.put("results", resultPages.size());
        json.put("pages", jsonArr);

        return json;
    }

    public static void main(String[] args) {
        System.out.println("Server is running...");
        port(PORT_NUMBER);
        get("/search", (req, res) -> {
            JSONParser parser = new JSONParser();
            JSONObject request = (JSONObject) parser.parse(req.body());
            String query = (String) request.get("query");
            String[] queries = query.split(" ");
            Vector<String> words = new Vector<>(Arrays.asList(queries));

            org.json.JSONObject response = processSearchQueries(words);
            res.type("application/json");
            res.status(200);
            return response;

        });
    }

}
