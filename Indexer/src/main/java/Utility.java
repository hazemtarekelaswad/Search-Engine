import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Utility {

    // Files utilities
    public static final String STOP_WORDS_FILE_PATH = "StopWords.txt";
    public static final String URLS_FILE_PATH = "Urls.txt";


    // Database utilities
    public static final String WORDS_COLLECTION = "words";
    public static final String PAGES_COLLECTION = "pages";
    private static final String CONNECTION_STRING = "mongodb+srv://hazemtarekelaswad:HazemSearchEngine@cluster0.4hpka.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
    private static final String DB_NAME = "searchIndexDB";
    private static MongoClient mongoClient;

    public static MongoCollection dbConnect(String collectionName) {
        ConnectionString connectionString = new ConnectionString(Utility.CONNECTION_STRING);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase(Utility.DB_NAME);

        MongoCollection collection = database.getCollection(collectionName);
        return collection;
    }

    public static void dbDisconnect() {
        mongoClient.close();
    }

    // Utility functions
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

}
