import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Arrays;
import java.util.Vector;

import static spark.Spark.*;

public class Api {
    private static final int PORT_NUMBER = 3000;

    public static void main(String[] args) {
        System.out.println("Server is running...");
        port(PORT_NUMBER);
        get("/search", (req, res) -> {
            JSONParser parser = new JSONParser();
            JSONObject request = (JSONObject) parser.parse(req.body());
            String query = (String) request.get("query");
            String[] queries = query.split(" ");
            Vector<String> words = new Vector<>(Arrays.asList(queries));

            Processor processor = new Processor();
            JSONObject response = processor.init(words);
            res.type("application/json");
            res.status(200);
            return response;

        });
    }
}
