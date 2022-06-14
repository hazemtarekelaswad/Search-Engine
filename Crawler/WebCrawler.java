package crawlproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler extends Thread {
    public static HashSet<String> firstLinks = new HashSet<String>();
    public static HashSet<String> outputLinks = new HashSet<String>();
    public static HashSet<String> Disallowed_links = new HashSet<String>();
    public final int MAXVISITS = 5000;

    public static Connection con;
    public static FileWriter fw1, fw2;

    public static void init() {

        try {
            FileWriter fw1 = new FileWriter("output.txt", true);
            String seedListPath = "B://Apt-Project//crawlproject//seedlist.txt/";
            File myFile1 = new File(seedListPath);
            Scanner myReader = new Scanner(myFile1);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                try {
                    firstLinks.add(data);
                    getRobotUrl(data);
                    System.out.println(" Disallow url" + Disallowed_links);

                } catch (Exception e) {
                    System.out.println("Error in reading seedlist.txt");
                }

            }
            myReader.close();
            fw1.close();
        } catch (Exception e) {
            System.out.println("Error in reaching to seedlist.txt");
        }
    }

    @Override
    public void run() {
        try {
            FileWriter fw1 = new FileWriter("seedlist.txt", true);
            FileWriter fw2 = new FileWriter("output.txt", true);
            while (outputLinks.size() < MAXVISITS) {
                // Want to allow only one thread to access the shared resource
                String crawlUrl = "";
                String normalizedUrl = "";

                synchronized (firstLinks) {
                    crawlUrl = firstLinks.iterator().next();
                    if (Disallowed_links.contains(crawlUrl)) {
                        // System.out.println("Disallow url: " + Disallowed_links );
                        continue;
                    }

                    // System.out.println("Thread number " + Thread.currentThread().getName() + "
                    // started");
                    // System.out.println("holding this link " + crawlUrl + " thread is :" +
                    // Thread.currentThread().getName());
                    firstLinks.remove(crawlUrl);
                }

                if (firstLinks.size() > 0) {
                    Document doc = Jsoup.connect(crawlUrl).get();
                    Elements linksOnPage = doc.select("a[href]");
                    for (Element page : linksOnPage) {
                        String absUrl = page.absUrl("href");

                        normalizedUrl = normalizeUrl(absUrl);

                        if (outputLinks.contains(normalizedUrl) || normalizedUrl.contains("javascript")
                                || normalizedUrl.contains("mailto")) {
                            continue;
                        } else {
                            outputLinks.add(normalizedUrl);
                        }

                        // Write in Output-file
                        fw2.append(normalizedUrl);
                        fw2.append("\n");
                        // can remove it in future
                        firstLinks.add(normalizedUrl);
                        // Write in seedlist
                        fw1.append(normalizedUrl);
                        fw1.append("\n");
                    }
                    firstLinks.remove(crawlUrl);
                    // remove from seed list
                }
            }
            fw1.close();
            fw2.close();
        } catch (IOException | URISyntaxException e) {
            // System.out.println("Uri error");
        }
    }

    public String normalizeUrl(String url) throws URISyntaxException {
        if (url == null) {
            return null;
        }
        if (url.indexOf('?') != -1)
            url = url.substring(0, url.indexOf('?'));
        if (url.indexOf('#') != -1)
            url = url.substring(0, url.indexOf('#'));
        URI uri = new URI(url);
        if (!uri.isAbsolute()) {
            throw new URISyntaxException(url, "Not an absolute URL");
        }
        uri = uri.normalize();
        String path = uri.getPath();
        if (path != null) {
            path = path.replaceAll("//*/", "/");
            if (path.length() > 0 && path.charAt(path.length() - 1) == '/')
                path = path.substring(0, path.length() - 1);
        }
        // return uri.toString();
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                uri.getPort(),
                path, uri.getQuery(), uri.getFragment()).toString();
    }

    public static void getRobotUrl(String url) throws MalformedURLException {
        try {
            if (url == null) {
                return;
            }
            URL fullUrl = new URL(url);
            String host = fullUrl.getHost(); // Get host and add on it robots.txt path
            String path = fullUrl.getPath(); // get path only that we want to remove it
            String protocolType = fullUrl.getProtocol(); // get protocol http or https
            String robotUrl = protocolType + "://" + host + "/robots.txt";
            URL fullRobotUrl = new URL(robotUrl);
            String notAccesable = null;

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(fullRobotUrl.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Compare if it is allow or disallow using length of string
                    String allowance = inputLine.substring(0, Math.min(inputLine.length(), 8));
                    if (allowance.equals("Disallow")) {
                        // Get the path that disallow crawling
                        String accessablePath = protocolType + "://" + host
                                + inputLine.substring(10, inputLine.length());

                        notAccesable = (accessablePath);
                    }
                }
                Disallowed_links.add(notAccesable); // put host and it's Path as a full

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Boolean checkRobotUrl(String url) throws MalformedURLException {
    // URL urlChecker = new URL(url);
    // String hostCheck = urlChecker.getHost();
    // List<String> notAllowedChecker = new ArrayList<String>();
    // notAllowedChecker = Disallowed_links.get(hostCheck);
    // // i = the number of elements in the list
    // for (int i = 0; i < notAllowedChecker.size(); i++) {
    // if (url.contains(notAllowedChecker.get(i))) {
    // System.out.println("This path can not crawled" + " " + url);
    // return true;
    // }
    // }
    // return false;
    // }

}
