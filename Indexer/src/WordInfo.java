public class WordInfo {

    private String name;
    private String tag;
    private String sentence;
    private PageInfo page;

    public WordInfo(String word, String tag, String sentence, PageInfo page) {
        this.name = word;
        this.tag = tag;
        this.sentence = sentence;
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public PageInfo getPage() {
        return page;
    }

    public void setPage(PageInfo page) {
        this.page = page;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
