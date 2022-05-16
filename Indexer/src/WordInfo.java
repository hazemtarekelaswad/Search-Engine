import java.util.HashSet;
import java.util.Vector;

public class WordInfo {

    private String name;
    private HashSet<String> tags;
    private HashSet<Integer> weights;
    private HashSet<String> sentences;
    private PageInfo page;
    private long termFreq;
    private long advTermFreq;

    public long getAdvTermFreq() {
        return advTermFreq;
    }

    public WordInfo(String word, String tag, String sentence, PageInfo page) {
        this.tags = new HashSet<>();
        this.weights = new HashSet<>();
        this.sentences = new HashSet<>();

        this.name = word;
        this.tags.add(tag);
        this.weights.add(getElementWeight(tag));

        this.sentences.add(sentence);
        this.page = page;

        this.termFreq = 1;
        this.advTermFreq = 1;
    }

    private Integer getElementWeight(String element) {
        switch (element) {
            case "h1": return 10;
            case "h2": return 9;
            case "h3": return 8;
            case "h4": return 7;
            case "h5": return 6;
            case "h6": return 5;
            case "p": return 4;
            case "a": return 3;
            case "li": return 2;
            case "span": return 1;
            default: return 0;
        }
    }

    public long getTermFreq() { return termFreq; }

    public void increaseTermFreq() { ++termFreq; }

    public void increaseTermWeight(String tag) {
        advTermFreq += getElementWeight(tag);
    }



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
        this.weights.add(getElementWeight(tag));
    }

    public HashSet<Integer> getWeights() {
        return weights;
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
