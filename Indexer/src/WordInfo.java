import java.util.HashSet;
import java.util.Vector;

public class WordInfo {

    private String name;
    private HashSet<String> tags;
    private HashSet<String> sentences;
    private PageInfo page;
    private long termFreq;

    public WordInfo(String word, String tag, String sentence, PageInfo page) {
        this.tags = new HashSet<>();
        this.sentences = new HashSet<>();

        this.name = word;
        this.tags.add(tag);
        this.sentences.add(sentence);
        this.page = page;

        this.termFreq = 1;
    }

    public long getTermFreq() { return termFreq; }

    public void increaseTermFreq() { ++termFreq; }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public PageInfo getPage() {
        return page;
    }

    public void setPage(PageInfo page) {
        this.page = page;
    }

    public HashSet<String> getSentences() {
        return sentences;
    }

    public void addSentence(String sentence) {
        this.sentences.add(sentence);
    }
}
