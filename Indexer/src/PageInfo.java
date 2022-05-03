import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

public class PageInfo {
    private String url;
    private String title;
    private Vector<WordInfo> words;


    public PageInfo(String url) throws IOException {
        this.url = url;
        this.words = new Vector<WordInfo>();

        Vector<String> stopWords = Utility.readFile(Utility.STOP_WORDS_FILE_PATH);

        Document doc = Jsoup.connect(url).get();

        this.title = doc.title();
        String body = doc.body().text();
        HashSet<String> refinedWords = new HashSet<>();
        String[] words = body.split("[ \t]");
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                refinedWords.add(word.toLowerCase());
            }
        }

        for (String word : refinedWords) {
            if (word.isEmpty() || word.contains("'") || word.contains("\"") || word.contains("\\") || word.contains("(") || word.contains(")") || word.contains("[") || word.contains("]") || word.contains("{") || word.contains("}"))
                continue;
            Elements elements = doc.select("*:matchesOwn(\\b" + word + "\\b)");
            for (Element element : elements) {
                String stemmedWord = Utility.stemWord(word);
                System.out.println(stemmedWord);
                WordInfo wordInfo = new WordInfo(stemmedWord, element.tagName(), element.text(), this);
                this.words.add(wordInfo);
            }
        }

    }

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
        long tf = 0;
        for (WordInfo w : words) {
            if (word.equals(w.getName())) ++tf;
        }
        return tf;
    }

    public double getNormTermFreq(String word) {
        return (double) getTermFreq(word) / words.size();
    }

    public HashSet<String> getTagsOf(String word) {
        HashSet<String> tags = new HashSet<>();
        for (WordInfo w : words) {
            if (word.equals(w.getName())) {
                tags.add(w.getTag());
            }
        }
        return tags;
    }

    public HashSet<String> getSentencesOf(String word) {
        HashSet<String> sentences = new HashSet<>();
        for (WordInfo w : words) {
            if (word.equals(w.getName())) {
                sentences.add(w.getSentence());
            }
        }
        return sentences;
    }


}
