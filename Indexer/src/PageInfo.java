import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;
import static com.mongodb.client.model.Filters.eq;

public class PageInfo {
    private String url;
    private String title;
    private Vector<WordInfo> words;
    private long rawWordsCount;

    private Vector<String> links;
    private long previousPopularityScore;
    private long currentPopularityScore;
    private long IDF;
    private long TF;

    public PageInfo(String url) throws IOException {
        this.url = url;
        this.words = new Vector<WordInfo>();
        this.links = new Vector<String>();

        Vector<String> stopWords = Utility.readFile(Utility.STOP_WORDS_FILE_PATH);

        Document doc = Jsoup.connect(url).get();

        /// TODO: get all links and fill links vector with

        Elements linkElements = doc.select("a[href]");  // HTML links only or not with http or not
        for (Element element : linkElements) {
            this.links.add(element.attr("href"));
        }

        this.title = doc.title();
        String body = doc.body().text();
        HashSet<String> refinedWords = new HashSet<>();
        String[] words = body.split("[ \t\n]");

        this.rawWordsCount = words.length;

        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                refinedWords.add(word.toLowerCase());
            }
        }

        for (String word : refinedWords) {
            if (word.isEmpty() || word.contains("'") || word.contains("\"") || word.contains("\\") || word.contains("(") || word.contains(")") || word.contains("[") || word.contains("]") || word.contains("{") || word.contains("}")) {
                continue;
            }
            Elements elements = doc.select("*:matchesOwn(\\b" + word + "\\b)");
            for (Element element : elements) {
                String stemmedWord = Utility.stemWord(word);
//                System.out.println(stemmedWord);

                boolean isFound = false;
                WordInfo tempWord = null;
                for (WordInfo w : this.words) {
                    if (w.getName().equals(stemmedWord)) {
                        isFound = true;
                        tempWord = w;
                        break;
                    }
                }
                if (isFound) {
                    tempWord.increaseTermFreq();
                    tempWord.increaseTermWeight(element.tagName());
                    tempWord.addTag(element.tagName());
                    tempWord.addSentence(element.text());
                } else {
                    WordInfo wordInfo = new WordInfo(stemmedWord, element.tagName(), element.text(), this);
                    this.words.add(wordInfo);
                }
            }
        }

    }

    public Vector<String> getLinks() throws IOException {
        this.links = new Vector<String>();

        Document doc = Jsoup.connect(this.url).get();

        Elements linkElements = doc.getElementsByTag("a");
        for (Element el : linkElements) {
            this.links.add(el.attr("href"));
        }

        System.out.println(this.links);

        return this.links;
    }

//    public void getTF(String word) {
//        long tf;
//        org.bson.Document wordDoc = (org.bson.Document) collection.find(eq("word", word.getName())).first();
//        this.TF = tf;
//
//    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Vector<WordInfo> getWords() {
        return words;
    }

    public void setWords(Vector<WordInfo> words) {
        this.words = words;
    }

    // Utility method
    private long getTermFreq(String word) {
        for (WordInfo w : this.words) {
            if (word.equals(w.getName())) return w.getTermFreq();
        }
        return -1;  // NOT FOUND
    }

    public double getNormTermFreq(String word) {
        long tf = getTermFreq(word);
        if (tf == -1) {
            System.err.println("The passed word is not found in this page");
            return -1;
        }
        return (double) tf / this.rawWordsCount;
    }

    private long getAdvTermFreq(String word) {
        for (WordInfo w : this.words) {
            if (word.equals(w.getName())) return w.getAdvTermFreq();
        }
        return -1;  // NOT FOUND
    }

    public double getNormAdvTermFreq(String word) {
        long advTf = getAdvTermFreq(word);
        if (advTf == -1) {
            System.err.println("The passed word is not found in this page");
            return -1;
        }
        return (double) advTf / this.rawWordsCount;
    }

    // TODO:
//    private long getRelevance(String word) {
//        for (WordInfo w : this.words) {
//            if (word.equals(w.getName())) return w.relevance;
//        }
//        return -1;  // NOT FOUND
//    }


//    public HashSet<String> getTagsOf(String word) {
//        HashSet<String> tags = new HashSet<>();
//        for (WordInfo w : words) {
//            if (word.equals(w.getName())) {
//                tags.add(w.getTag());
//            }
//        }
//        return tags;
//    }

//    public HashSet<String> getSentencesOf(String word) {
//        HashSet<String> sentences = new HashSet<>();
//        for (WordInfo w : words) {
//            if (word.equals(w.getName())) {
//                sentences.add(w.getSentence());
//            }
//        }
//        return sentences;
//    }


}
