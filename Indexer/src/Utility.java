import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.lucene.analysis.stempel.*;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Utility {
    public static final String STOP_WORDS_FILE_PATH = "StopWords.txt";
    public static final String URLS_FILE_PATH = "Urls.txt";

    private static final String CONNECTION_STRING = "mongodb+srv://hazemtarekelaswad:HazemSearchEngine@cluster0.4hpka.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
    private static final String DB_NAME = "searchIndexDB";
    private static final String MAIN_COLLECTION = "words";
    private static MongoClient mongoClient;

    public static Vector<String> readFile(String filePath) throws FileNotFoundException {
        Vector<String> lines = new Vector<String>();
        Scanner scanner = new Scanner(new File(filePath));
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();
        return lines;
    }

    public static String stemWord(String word) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        TokenStream stream = analyzer.tokenStream("field", word);
        stream.reset();
        String stemmedWord = "";
        while (stream.incrementToken()) {
            stemmedWord = stream.getAttribute(CharTermAttribute.class).toString();
        }
        stream.end();
        stream.close();
        return stemmedWord;
    }

    // returns the main collection
    public static MongoCollection dbConnect() {
        ConnectionString connectionString = new ConnectionString(Utility.CONNECTION_STRING);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase(Utility.DB_NAME);

        MongoCollection collection = database.getCollection(Utility.MAIN_COLLECTION);
        return collection;
    }

    public static void dbDisconnect() {
        mongoClient.close();
    }

    public static void main(String[] args) throws IOException {
        // You can test any utility function here.
    }
}
