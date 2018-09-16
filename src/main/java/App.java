import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static HttpRequestFactory factory = new NetHttpTransport().createRequestFactory();

    public static void main(String[] args) {
        var urls = Arrays.asList("https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample",
                                 "https://google.com/?q=sample");

        parallelStreamCustomPool(urls);
    }

    private static void sequentially(List<String> urls) {
        logger.debug("Starting fetching sequentially...");
        urls.stream()
            .map(App::makeRequest)
            .forEach(r -> {
                logger.debug("Got response of length {} from makeRequest", r.length());
            });
        logger.debug("Done fetching sequentially");
    }

    private static void parallelStream(List<String> urls) {
        logger.debug("Starting fetching parallelStream...");
        urls.parallelStream()
            .map(App::makeRequest)
            .forEach(r -> {
                logger.debug("Got response of length {} from makeRequest", r.length());
            });
        logger.debug("Done fetching parallelStream");
    }

    private static void parallelStreamCustomPool(List<String> urls) {
        logger.debug("Starting fetching parallelStream with custom pool...");
        ForkJoinPool fkPool = new ForkJoinPool(10);
        ForkJoinTask fkTask = fkPool.submit(() -> {
            urls.parallelStream()
                .map(App::makeRequest)
                .forEach(r -> {
                    logger.debug("Got response of length {} from makeRequest", r.length());
                });
        });

        try {
            fkTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        logger.debug("Done fetching parallelStream with custom pool");
    }

    private static String makeRequest(String u) {
        var url = new GenericUrl(u);
        logger.debug("Fetching from {}", url);
        try {
            var request = factory.buildGetRequest(url);
            return request.execute().parseAsString();
        } catch (IOException e) {
            logger.error("Error fetching from {}", url, e);
            return "";
        }
    }
}
