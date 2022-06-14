package crawlproject;

public class Crawler_Main {
    static int ThreadsNum;

    public static void main(String[] args) throws Exception {
        System.out.println("Start WebCrawler");
        // Initialize Crawler Parameters
        WebCrawler.init();

        // get ThreadsNumber
        int ThreadsNum = 6;

        // Create Threads
        WebCrawler[] threads = new WebCrawler[ThreadsNum];
        for (int i = 0; i < ThreadsNum; i++) {
            threads[i] = new WebCrawler();
            threads[i].start();
            threads[i].setName(String.valueOf(i));
        }
        for (int i = 0; i < ThreadsNum; i++) {
            threads[i].join();
            System.out.println("Thread number " + i + " Finished");
        }
        // Finished crawling
        int count = WebCrawler.firstLinks.size();
        System.out.println("Number of files:" + count);

    }
}