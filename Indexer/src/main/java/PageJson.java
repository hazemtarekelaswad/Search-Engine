public class PageJson {
    public String url;
    public String title;
    public String paragraph;
    public Double score;

    public PageJson() {
        this.url = null;
        this.title = null;
        this.paragraph = null;
        this.score = 0.0;
    }

    public PageJson(String url, String title, String paragraph, Double score) {
        this.url = url;
        this.title = title;
        this.paragraph = paragraph;
        this.score = score;
    }
}